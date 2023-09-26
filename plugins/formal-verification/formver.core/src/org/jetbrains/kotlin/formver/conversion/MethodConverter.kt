/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.formver.embeddings.ExpEmbedding
import org.jetbrains.kotlin.formver.embeddings.VariableEmbedding
import org.jetbrains.kotlin.formver.embeddings.callables.FunctionSignature
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.Label
import org.jetbrains.kotlin.name.Name

/**
 * The symbol resolution data for a single method.
 *
 * Method converters are chained syntactically; the converter of a lambda has the method that the lambda is defined in as a parent.
 * In general, however, a callee inline function does *not* in general have its caller as a parent: this is because an inlined
 * function does not have access to the variables of its caller, so it does not make sense to have symbol resolution pass through it.
 *
 * We're using the term `MethodConverter` here for consistency with the `XConverter` implementing `XConversionContext`.
 * Really, this class doesn't do any conversion itself, it just provides information for the `StmtConverter`
 * to get its work done.
 */
class MethodConverter(
    private val programCtx: ProgramConversionContext,
    override val signature: FunctionSignature,
    private val paramResolver: ParameterResolver,
    scopeDepth: Int,
    private val parent: MethodConversionContext? = null,
) : MethodConversionContext, ProgramConversionContext by programCtx {
    private var propertyResolver = PropertyNameResolver(scopeDepth)

    override fun <R> withScopeImpl(scopeDepth: Int, action: () -> R): R {
        propertyResolver = propertyResolver.innerScope(scopeDepth)
        val result = action()
        propertyResolver = propertyResolver.parent!!
        return result
    }

    override fun resolveLocalPropertyName(name: Name): MangledName =
        propertyResolver.tryResolveLocalPropertyName(name) ?: parent?.resolveLocalPropertyName(name)
        ?: throw IllegalArgumentException("Property $name not found in scope.")

    override fun registerLocalPropertyName(name: Name) {
        propertyResolver.registerLocalPropertyName(name)
    }

    override fun embedParameter(symbol: FirValueParameterSymbol): ExpEmbedding =
        paramResolver.tryEmbedParameter(symbol) ?: parent?.embedParameter(symbol)
        ?: throw IllegalArgumentException("Parameter $symbol not found in scope.")

    override val resolvedReturnVarName: MangledName = paramResolver.resolvedReturnVarName
    override val resolvedReturnLabelName: ReturnLabelName = paramResolver.resolvedReturnLabelName

    override fun getReturnVar(sourceName: String?, isLambda: Boolean): VariableEmbedding {
        val returnPoint = if (sourceName != null) {
            ReturnPoint(sourceName, isLambda)
        } else {
            null
        }
        val name = paramResolver.returnPointResolver[returnPoint]?.resolvedReturnVarName ?: resolvedReturnVarName
        return VariableEmbedding(name, signature.returnType)
    }

    override fun getReturnLabel(sourceName: String?, isLambda: Boolean): Label {
        val returnPoint = if (sourceName != null) {
            ReturnPoint(sourceName, isLambda)
        } else {
            null
        }
        val name = paramResolver.returnPointResolver[returnPoint]?.resolvedReturnLabelName ?: resolvedReturnLabelName
        return Label(name, listOf())
    }

    override fun addReturnPoint(sourceName: String, isLambda: Boolean) {
        val returnPoint = ReturnPoint(sourceName, isLambda)
        paramResolver.returnPointResolver[returnPoint] = ReturnTarget(resolvedReturnVarName, resolvedReturnLabelName)
    }

    override fun getReturnPoints(): Map<ReturnPoint, ReturnTarget> = paramResolver.returnPointResolver
}