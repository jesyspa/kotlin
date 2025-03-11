/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.fir.FirLabel
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.symbol
import org.jetbrains.kotlin.fir.symbols.impl.FirIntersectionOverridePropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirVariableSymbol
import org.jetbrains.kotlin.fir.types.isBoolean
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.formver.embeddings.*
import org.jetbrains.kotlin.formver.embeddings.callables.FullNamedFunctionSignature
import org.jetbrains.kotlin.formver.embeddings.types.TypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.callables.FunctionSignature
import org.jetbrains.kotlin.formver.embeddings.expression.*
import org.jetbrains.kotlin.formver.isCustom
import org.jetbrains.kotlin.formver.linearization.Linearizer
import org.jetbrains.kotlin.formver.linearization.SeqnBuilder
import org.jetbrains.kotlin.formver.linearization.SharedLinearizationState
import org.jetbrains.kotlin.formver.shouldBeInlined
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.delegateToCallable
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addIfNotNull
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import org.jetbrains.kotlin.utils.filterIsInstanceAnd

/**
 * Interface for statement conversion.
 *
 * Naming convention:
 * - Functions that return a new `StmtConversionContext` should describe what change they make (`addResult`, `removeResult`...)
 * - Functions that take a lambda to execute should describe what extra state the lambda will have (`withResult`...)
 */
interface StmtConversionContext : MethodConversionContext {
    val whenSubject: VariableEmbedding?

    /**
     * In a safe call `callSubject?.foo()` we evaluate the call subject first to check for nullness.
     * In case it is not null, we evaluate the call to `callSubject.foo()`. Here we don't want to evaluate
     * the `callSubject` again to we store it in the `StmtConversionContext`.
     */
    val checkedSafeCallSubject: ExpEmbedding?
    val activeCatchLabels: List<LabelEmbedding>

    fun continueLabelName(targetName: String? = null): MangledName
    fun breakLabelName(targetName: String? = null): MangledName
    fun addLoopName(targetName: String)
    fun convert(stmt: FirStatement): ExpEmbedding

    fun <R> withNewScope(action: StmtConversionContext.() -> R): R
    fun <R> withMethodCtx(factory: MethodContextFactory, action: StmtConversionContext.() -> R): R

    fun <R> withFreshWhile(label: FirLabel?, action: StmtConversionContext.() -> R): R
    fun <R> withWhenSubject(subject: VariableEmbedding?, action: StmtConversionContext.() -> R): R
    fun <R> withCheckedSafeCallSubject(subject: ExpEmbedding?, action: StmtConversionContext.() -> R): R
    fun <R> withCatches(
        catches: List<FirCatch>,
        action: StmtConversionContext.(catchBlockListData: CatchBlockListData) -> R,
    ): Pair<CatchBlockListData, R>
}

fun StmtConversionContext.declareLocalProperty(symbol: FirPropertySymbol, initializer: ExpEmbedding?): Declare {
    registerLocalProperty(symbol)
    return Declare(embedLocalProperty(symbol), initializer)
}

fun StmtConversionContext.declareLocalVariable(symbol: FirVariableSymbol<*>, initializer: ExpEmbedding?): Declare {
    registerLocalVariable(symbol)
    return Declare(embedLocalVariable(symbol), initializer)
}

fun StmtConversionContext.declareAnonVar(type: TypeEmbedding, initializer: ExpEmbedding?): Declare =
    Declare(freshAnonVar(type), initializer)


val FirIntersectionOverridePropertySymbol.propertyIntersections
    get() = intersections.filterIsInstanceAnd<FirPropertySymbol> { it.isVal == isVal }

/**
 * Tries to find final property symbol actually declared in some class instead of
 * (potentially) fake property symbol.
 * Note that if some property is found it is fixed since
 * 1. there can't be two non-abstract properties which don't subsume each other
 * in the hierarchy (kotlin disallows that) and final properties can't be abstract;
 * 2. final property can't subsume other final property as that means final property
 * is overridden.
 * //TODO: decide if we leave this lookup or consider it unsafe.
 */
fun FirPropertySymbol.findFinalParentProperty(): FirPropertySymbol? =
    if (this !is FirIntersectionOverridePropertySymbol)
        (isFinal && !isCustom).ifTrue { this }
    else propertyIntersections.firstNotNullOfOrNull { it.findFinalParentProperty() }


/**
 * This is a key function when looking up properties.
 * It translates a kotlin `receiver.field` expression to an `ExpEmbedding`.
 *
 * Note that in FIR this `field` may be represented as `FirIntersectionOverridePropertySymbol`
 * which is necessary when the property could hypothetically inherit from multiple sources.
 * However, we don't register such symbols in the context when traversing the class.
 * Hence, some advanced logic is needed here.
 *
 * First, we try to find an actual backing field somewhere in the parents of the field with a
 * dfs-like algorithm on `FirIntersectionOverridePropertySymbol`s (it also should be final).
 *
 * If final backing field is not found, we lazily create a getter/setter pair for this
 * `FirIntersectionOverrideProperty`.
 */
fun StmtConversionContext.embedPropertyAccess(accessExpression: FirPropertyAccessExpression): PropertyAccessEmbedding =
    when (val calleeSymbol = accessExpression.calleeReference.symbol) {
        is FirValueParameterSymbol -> embedParameter(calleeSymbol).asPropertyAccess()
        is FirPropertySymbol -> {
            val type = embedType(calleeSymbol.resolvedReturnType)
            when {
                accessExpression.dispatchReceiver != null -> {
                    val property = calleeSymbol.findFinalParentProperty()?.let {
                        embedProperty(it)
                    } ?: embedProperty(calleeSymbol)
                    ClassPropertyAccess(convert(accessExpression.dispatchReceiver!!), property, type)
                }
                accessExpression.extensionReceiver != null -> {
                    val property = embedProperty(calleeSymbol)
                    ClassPropertyAccess(convert(accessExpression.extensionReceiver!!), property, type)
                }
                else -> embedLocalProperty(calleeSymbol)
            }
        }
        else ->
            error("Property access symbol $calleeSymbol has unsupported type.")
    }


fun StmtConversionContext.argumentDeclaration(arg: ExpEmbedding, callType: TypeEmbedding): Pair<Declare?, ExpEmbedding> =
    when (arg.ignoringMetaNodes()) {
        is LambdaExp -> null to arg
        else -> {
            val argWithInvariants = arg.withNewTypeInvariants(callType) {
                proven = true
                access = true
            }
            // If `argWithInvariants` is `Cast(...(Cast(someVariable))...)` it is fine to use it
            // since in Viper it will always be translated to `someVariable`.
            // On other hand, `TypeEmbedding` and invariants in Viper are guaranteed
            // via previous line.
            if (argWithInvariants.underlyingVariable != null) null to argWithInvariants
            else declareAnonVar(callType, argWithInvariants).let {
                it to it.variable
            }
        }
    }

fun StmtConversionContext.getInlineFunctionCallArgs(
    args: List<ExpEmbedding>,
    formalArgTypes: List<TypeEmbedding>,
): Pair<List<Declare>, List<ExpEmbedding>> {
    val declarations = mutableListOf<Declare>()
    val storedArgs = args.zip(formalArgTypes).map { (arg, callType) ->
        argumentDeclaration(arg, callType).let { (declaration, usage) ->
            declarations.addIfNotNull(declaration)
            usage
        }
    }
    return Pair(declarations, storedArgs)
}

fun StmtConversionContext.insertInlineFunctionCall(
    calleeSignature: FunctionSignature,
    paramNames: List<SubstitutedArgument>,
    args: List<ExpEmbedding>,
    body: FirBlock,
    returnTargetName: String?,
    parentCtx: MethodConversionContext? = null,
): ExpEmbedding {
    // TODO: It seems like it may be possible to avoid creating a local here, but it is not clear how.
    val returnTarget = returnTargetProducer.getFresh(calleeSignature.callableType.returnType)
    val (declarations, callArgs) = getInlineFunctionCallArgs(args, calleeSignature.callableType.formalArgTypes)
    val subs = paramNames.zip(callArgs).toMap()
    val methodCtxFactory = MethodContextFactory(
        calleeSignature,
        InlineParameterResolver(subs, returnTargetName, returnTarget),
        parent = parentCtx,
    )
    return withMethodCtx(methodCtxFactory) {
        Block {
            add(Declare(returnTarget.variable, null))
            addAll(declarations)
            add(FunctionExp(null, convert(body), returnTarget.label))
            // if unit is what we return we might not guarantee it yet
            add(returnTarget.variable.withIsUnitInvariantIfUnit())
        }
    }
}

fun StmtConversionContext.convertMethodWithBody(
    declaration: FirSimpleFunction,
    signature: FullNamedFunctionSignature,
    returnTarget: ReturnTarget,
): FunctionBodyEmbedding? {
    val firBody = declaration.body ?: return null
    val body = convert(firBody)
    val bodyExp = FunctionExp(signature, body, returnTarget.label)
    val seqnBuilder = SeqnBuilder(declaration.source)
    val linearizer = Linearizer(SharedLinearizationState(anonVarProducer), seqnBuilder, declaration.source)
    bodyExp.toViperUnusedResult(linearizer)
    // note: we must guarantee somewhere that returned value is Unit
    // as we may not encounter any `return` statement in the body
    returnTarget.variable.withIsUnitInvariantIfUnit().toViperUnusedResult(linearizer)
    return FunctionBodyEmbedding(seqnBuilder.block, returnTarget, bodyExp)
}

private const val INVALID_STATEMENT_MSG =
    "Every statement in invariant block must be a pure boolean invariant."

fun StmtConversionContext.collectInvariants(block: FirBlock) = buildList {
    block.statements.forEach { stmt ->
        check(stmt is FirExpression && stmt.resolvedType.isBoolean) {
            INVALID_STATEMENT_MSG
        }
        add(stmt.accept(StmtConversionVisitor, this@collectInvariants))
    }
}

