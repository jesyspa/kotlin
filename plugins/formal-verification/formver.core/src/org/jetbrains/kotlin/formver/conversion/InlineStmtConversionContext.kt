/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.formver.embeddings.TypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.VariableEmbedding
import org.jetbrains.kotlin.formver.scala.MangledName

class InlineStmtConversionContext(
    private val methodCtx: MethodConversionContext,
    private val substitutionParams: Map<MangledName, MangledName>,
    val returnVar: VariableEmbedding
) :
    StmtConverter(methodCtx) {
    private var nextAnonVarNumber = 0
    fun getName(name: MangledName): MangledName = substitutionParams[name] ?: InlineName(methodCtx.signature.name.mangled, name.mangled)

    override fun newAnonVar(type: TypeEmbedding): VariableEmbedding =
        VariableEmbedding(InlineName(methodCtx.signature.name.mangled, AnonymousName(++nextAnonVarNumber).mangled), type)
}