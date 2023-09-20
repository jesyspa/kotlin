/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.formver.embeddings.callables.FullNamedFunctionSignature
import org.jetbrains.kotlin.formver.embeddings.embedName
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.name.Name

sealed interface SubstitutionItem {
    val name: MangledName?
    fun lambdaBody(): FirBlock? = null
    fun lambdaArgs(): List<Name>? = null
}

data class SubstitutionName(override val name: MangledName) : SubstitutionItem

data class SubstitutionLambda(val body: FirBlock, val args: List<Name>, val scopedNames: Map<Name, Int>) : SubstitutionItem {
    override val name = null
    override fun lambdaBody(): FirBlock = body
    override fun lambdaArgs(): List<Name> = args
}

class InlineNameMangler(
    override val mangledReturnValueName: MangledName,
    private val substitutionParams: Map<Name, SubstitutionItem>,
    returnLabelIndex: Int,
) :
    NameMangler {
    override fun mangleParameterName(parameter: FirValueParameterSymbol): MangledName =
        substitutionParams[parameter.name]?.name ?: parameter.embedName()

    override fun mangleLocalPropertyName(property: FirPropertySymbol, scopeDepth: Int): MangledName =
        NoopNameMangler.mangleLocalPropertyName(property, scopeDepth)

    override val mangledReturnLabelName: MangledName =
        InlineReturnLabelName(returnLabelIndex)
}

class InlineMethodConverter(
    private val programCtx: ProgramConversionContext,
    override val signature: FullNamedFunctionSignature,
    returnVarName: MangledName,
    private val substitutionParams: Map<Name, SubstitutionItem>,
    scopeDepth: Int
) : MethodConversionContext, ProgramConversionContext by programCtx {
    override val nameMangler = InlineNameMangler(returnVarName, substitutionParams, scopeDepth)

    override fun getLambdaOrNull(name: Name): SubstitutionLambda? = substitutionParams[name] as? SubstitutionLambda
}