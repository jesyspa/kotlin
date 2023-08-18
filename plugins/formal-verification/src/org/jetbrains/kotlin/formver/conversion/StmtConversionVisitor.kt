/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.contracts.description.LogicOperationKind
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.impl.FirElseIfTrueCondition
import org.jetbrains.kotlin.fir.expressions.impl.FirNoReceiverExpression
import org.jetbrains.kotlin.fir.references.resolved
import org.jetbrains.kotlin.fir.references.toResolvedBaseSymbol
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.coneTypeOrNull
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.formver.embeddings.*
import org.jetbrains.kotlin.formver.scala.silicon.ast.Exp
import org.jetbrains.kotlin.formver.scala.silicon.ast.Stmt
import org.jetbrains.kotlin.text
import org.jetbrains.kotlin.types.ConstantValueKind

/**
 * Convert a statement, emitting the resulting Viper statements and
 * declarations into the context, returning a reference to the
 * expression containing the result.  Note that in the FIR, expressions
 * are a subtype of statements.
 *
 * In many cases, we introduce a temporary variable to represent this
 * result (since, for example, a method call is not an expression).
 * When the result is an lvalue, it is important to return an expression
 * that refers to location, not just the same value, and so introducing
 * a temporary variable for the result is not acceptable in those cases.
 */
class StmtConversionVisitor : FirVisitor<ExpEmbedding, StmtConversionContext>() {
    // Note that in some cases we don't expect to ever implement it: we are only
    // translating statements here, after all.  It isn't 100% clear how best to
    // communicate this.
    override fun visitElement(element: FirElement, data: StmtConversionContext): ExpEmbedding =
        TODO("Not yet implemented for $element (${element.source.text})")

    override fun visitReturnExpression(returnExpression: FirReturnExpression, data: StmtConversionContext): ExpEmbedding {
        val expr = returnExpression.result.accept(this, data)
        // TODO: respect return-based control flow
        val returnVar = data.signature.returnVar
        data.addStatement(Stmt.LocalVarAssign(returnVar.toLocalVar(), expr.withType(returnVar.type).viperExp))
        return UnitLit
    }

    override fun visitBlock(block: FirBlock, data: StmtConversionContext): ExpEmbedding =
        // We ignore the accumulator: we just want to get the result of the last expression.
        block.statements.fold<FirStatement, ExpEmbedding>(UnitLit) { _, it -> it.accept(this, data) }

    override fun <T> visitConstExpression(constExpression: FirConstExpression<T>, data: StmtConversionContext): ExpEmbedding =
        when (constExpression.kind) {
            ConstantValueKind.Int -> IntLit((constExpression.value as Long).toInt())
            ConstantValueKind.Boolean -> BoolLit(constExpression.value as Boolean)
            ConstantValueKind.Null -> NullLit(data.embedType(constExpression.typeRef.coneType) as NullableTypeEmbedding)
            else -> TODO("Constant Expression of type ${constExpression.kind} is not yet implemented.")
        }

    override fun visitWhenSubjectExpression(whenSubjectExpression: FirWhenSubjectExpression, data: StmtConversionContext): ExpEmbedding =
        // TODO: find a way to not evaluate subject multiple times if it is a function call
        whenSubjectExpression.whenRef.value.subject!!.accept(this, data)

    private fun convertWhenBranches(whenBranches: Iterator<FirWhenBranch>, data: StmtConversionContext, cvar: VariableEmbedding?) {
        // NOTE: I think that this will also work with "in" or "is" conditions when implemented, but I'm not 100% sure
        if (!whenBranches.hasNext()) return

        val branch = whenBranches.next()

        // Note that only the last condition can be a FirElseIfTrue
        if (branch.condition is FirElseIfTrueCondition) {
            val result = branch.result.accept(this, data)
            cvar?.let { data.addStatement(Stmt.LocalVarAssign(cvar.toLocalVar(), result.viperExp)) }
        } else {
            val cond = branch.condition.accept(this, data)
            val thenCtx = StmtConverter(data)
            // TODO: Convert branch expressions into the correct type:
            // Ideally we want to something like `thenResult.withType(data.embedType(whenExpression.typeRef.coneType)),
            // however, this is not straight forward when for example the if contains returns and the type of the when is Nothing.
            val thenResult = branch.result.accept(this, thenCtx)
            cvar?.let { thenCtx.addStatement(Stmt.LocalVarAssign(cvar.toLocalVar(), thenResult.viperExp)) }
            val elseCtx = StmtConverter(data)
            convertWhenBranches(whenBranches, elseCtx, cvar)
            data.addStatement(Stmt.If(cond.viperExp, thenCtx.block, elseCtx.block))
        }
    }

    override fun visitWhenExpression(whenExpression: FirWhenExpression, data: StmtConversionContext): ExpEmbedding {
        val cvar = if (whenExpression.usedAsExpression) {
            data.newAnonVar(data.embedType(whenExpression.typeRef.coneTypeOrNull!!))
        } else {
            null
        }
        cvar?.let { data.addDeclaration(cvar.toLocalVarDecl()) }
        convertWhenBranches(whenExpression.branches.iterator(), data, cvar)
        return cvar ?: UnitLit
    }

    override fun visitPropertyAccessExpression(
        propertyAccessExpression: FirPropertyAccessExpression,
        data: StmtConversionContext,
    ): ExpEmbedding {
        val symbol = propertyAccessExpression.calleeReference.toResolvedBaseSymbol()!!
        val type = propertyAccessExpression.typeRef.coneTypeOrNull!!
        return when (symbol) {
            is FirValueParameterSymbol -> VariableEmbedding(
                symbol.callableId.embedName(),
                data.embedType(type)
            )
            is FirPropertySymbol -> VariableEmbedding(
                symbol.callableId.embedName(),
                data.embedType(type)
            )
            else -> TODO("Implement other property accesses")
        }
    }

    override fun visitEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall, data: StmtConversionContext): ExpEmbedding {
        if (equalityOperatorCall.arguments.size != 2) {
            throw IllegalArgumentException("Invalid equality comparison $equalityOperatorCall, can only compare 2 elements.")
        }
        val left = equalityOperatorCall.arguments[0].accept(this, data)
        val right = equalityOperatorCall.arguments[1].accept(this, data)

        return when (equalityOperatorCall.operation) {
            FirOperation.EQ -> EqCmp(left, right)
            FirOperation.NOT_EQ -> NeCmp(left, right)
            else -> TODO("Equality comparison operation ${equalityOperatorCall.operation} not yet implemented.")
        }
    }

    override fun visitFunctionCall(functionCall: FirFunctionCall, data: StmtConversionContext): ExpEmbedding {
        val id = functionCall.calleeReference.toResolvedCallableSymbol()!!.callableId
        val specialFunc = SpecialFunctions.byCallableId[id]
        // TODO: Convert the arguments to the types that the function expects with `withType`.
        val getArgs = { getFunctionCallArguments(functionCall).map { it.accept(this, data) } }
        if (specialFunc != null) {
            if (specialFunc !is SpecialFunctionImplementation) return UnitLit
            return specialFunc.convertCall(getArgs(), data)
        }

        val args = getArgs()
        val symbol = functionCall.calleeReference.resolved!!.resolvedSymbol as FirNamedFunctionSymbol
        val calleeSig = data.add(symbol)
        val returnVar = data.newAnonVar(calleeSig.returnType)
        data.addDeclaration(returnVar.toLocalVarDecl())
        data.addStatement(Stmt.MethodCall(calleeSig.name.mangled, args.viperExps(), listOf(returnVar.toLocalVar())))
        return returnVar
    }

    private fun getFunctionCallArguments(functionCall: FirFunctionCall): List<FirExpression> {
        // I'm sure there's a nicer and more functional way of writing this, feel free to
        // refactor if you know how. :)
        val receiver = if (functionCall.dispatchReceiver !is FirNoReceiverExpression) {
            listOf(functionCall.dispatchReceiver)
        } else {
            emptyList()
        }
        return receiver + functionCall.argumentList.arguments
    }

    override fun visitProperty(property: FirProperty, data: StmtConversionContext): ExpEmbedding {
        val symbol = property.symbol
        val type = property.returnTypeRef.coneTypeOrNull!!
        if (!symbol.isLocal) {
            TODO("Implement non-local properties")
        }
        val varType = data.embedType(type)
        val cvar = VariableEmbedding(symbol.callableId.embedName(), varType)
        val propInitializer = property.initializer
        val initializer = propInitializer?.accept(this, data)
        data.addDeclaration(cvar.toLocalVarDecl())
        initializer?.let { data.addStatement(Stmt.LocalVarAssign(cvar.toLocalVar(), it.withType(varType).viperExp)) }
        return UnitLit
    }

    override fun visitWhileLoop(whileLoop: FirWhileLoop, data: StmtConversionContext): ExpEmbedding {
        val cond = whileLoop.condition.accept(this, data)
        val invariants: List<Exp> = emptyList()
        val bodyStmtConversionContext = StmtConverter(data)
        bodyStmtConversionContext.convertAndAppend(whileLoop.block)
        val body = bodyStmtConversionContext.block
        data.addStatement(Stmt.While(cond.viperExp, invariants, body))
        return UnitLit
    }

    override fun visitVariableAssignment(variableAssignment: FirVariableAssignment, data: StmtConversionContext): ExpEmbedding {
        // It is not entirely clear whether we can get away with ignoring the distinction between
        // lvalues and rvalues, but let's try to at first, and we'll fix it later if it turns out
        // not to work.
        val convertedLValue = variableAssignment.lValue.accept(this, data)
        val convertedRValue = variableAssignment.rValue.accept(this, data)
        data.addStatement(Stmt.assign(convertedLValue.viperExp, convertedRValue.withType(convertedLValue.type).viperExp))
        return UnitLit
    }

    override fun visitSmartCastExpression(smartCastExpression: FirSmartCastExpression, data: StmtConversionContext): ExpEmbedding {
        val exp = smartCastExpression.originalExpression.accept(this, data)
        val newType = smartCastExpression.smartcastType.coneType
        return exp.withType(data.embedType(newType))
    }

    override fun visitBinaryLogicExpression(binaryLogicExpression: FirBinaryLogicExpression, data: StmtConversionContext): ExpEmbedding {
        val returnVar = data.newAnonVar(BooleanTypeEmbedding)
        data.addDeclaration(returnVar.toLocalVarDecl())
        val left = binaryLogicExpression.leftOperand.accept(this, data)
        val rightSubStmt = StmtConverter(data)
        val right = binaryLogicExpression.rightOperand.accept(this, rightSubStmt)
        rightSubStmt.addStatement(Stmt.LocalVarAssign(returnVar.toLocalVar(), right.viperExp))
        when (binaryLogicExpression.kind) {
            LogicOperationKind.AND ->
                data.addStatement(
                    Stmt.If(
                        left.viperExp,
                        rightSubStmt.block,
                        Stmt.Seqn(listOf(Stmt.LocalVarAssign(returnVar.toLocalVar(), Exp.BoolLit(false))), listOf())
                    )
                )
            LogicOperationKind.OR ->
                data.addStatement(
                    Stmt.If(
                        left.viperExp,
                        Stmt.Seqn(listOf(Stmt.LocalVarAssign(returnVar.toLocalVar(), Exp.BoolLit(true))), listOf()),
                        rightSubStmt.block
                    )
                )
        }
        return returnVar
    }
}