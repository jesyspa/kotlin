/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.callables

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.formver.asPosition
import org.jetbrains.kotlin.formver.embeddings.expression.*
import org.jetbrains.kotlin.formver.linearization.pureToViperCondition
import org.jetbrains.kotlin.formver.viper.ast.Stmt
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.UserMethod

interface FullNamedFunctionSignature : NamedFunctionSignature {
    /**
     * Preconditions of function in form of `ExpEmbedding`s with type `boolType()`.
     */
    fun getEmbeddingPreconditions(returnVariable: VariableEmbedding): List<ExpEmbedding>

    /**
     * Postconditions of function in form of `ExpEmbedding`s with type `boolType()`.
     */
    fun getEmbeddingPostconditions(returnVariable: VariableEmbedding): List<ExpEmbedding>

    /**
     * Preconditions of function in form of Viper `Exp`s of type `Bool`.
     */
    fun getViperPreconditions(returnVariable: VariableEmbedding): List<Exp> =
        getEmbeddingPreconditions(returnVariable).pureToViperCondition()

    /**
     * Postconditions of function in form of Viper `Exp`s of type `Bool`.
     */
    fun getViperPostconditions(returnVariable: VariableEmbedding): List<Exp> =
        getEmbeddingPostconditions(returnVariable).pureToViperCondition()

    val declarationSource: KtSourceElement?
}

fun FullNamedFunctionSignature.toViperMethod(
    body: Stmt.Seqn?,
    returnVariable: VariableEmbedding,
) = UserMethod(
    name,
    formalArgs.map { it.toLocalVarDecl() },
    returnVariable.toLocalVarDecl(),
    getViperPreconditions(returnVariable),
    getViperPostconditions(returnVariable),
    body,
    declarationSource.asPosition
)