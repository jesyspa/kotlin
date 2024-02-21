/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.callables

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.formver.asPosition
import org.jetbrains.kotlin.formver.conversion.AccessPolicy
import org.jetbrains.kotlin.formver.embeddings.ClassTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.PrimaryConstructorFieldEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.*
import org.jetbrains.kotlin.formver.linearization.pureToViper
import org.jetbrains.kotlin.formver.names.ParameterScope
import org.jetbrains.kotlin.formver.names.ScopedKotlinName
import org.jetbrains.kotlin.formver.names.SimpleKotlinName
import org.jetbrains.kotlin.formver.viper.ast.Stmt
import org.jetbrains.kotlin.formver.viper.ast.UserMethod

interface FullNamedFunctionSignature : NamedFunctionSignature {
    fun getPreconditions(returnVariable: VariableEmbedding): List<ExpEmbedding>
    fun getPostconditions(returnVariable: VariableEmbedding): List<ExpEmbedding>
    val declarationSource: KtSourceElement?
}

interface PrimaryConstructorFunctionSignature : FullNamedFunctionSignature {
    private fun primaryConstructorFieldsWithParams(): List<Pair<PrimaryConstructorFieldEmbedding, VariableEmbedding>> {
        val fields = (returnType as? ClassTypeEmbedding)?.fields?.toList() ?: return emptyList()
        return fields.filterIsInstance<Pair<SimpleKotlinName, PrimaryConstructorFieldEmbedding>>().map { (name, field) ->
            val correspondingParam = params.find { ScopedKotlinName(ParameterScope, name) == it.name }
            field to correspondingParam
        }.filterIsInstance<Pair<PrimaryConstructorFieldEmbedding, VariableEmbedding>>()
    }

    private fun readonlyPrimaryConstructorFieldsWithParams(): List<Pair<PrimaryConstructorFieldEmbedding, VariableEmbedding>> =
        primaryConstructorFieldsWithParams().filter { (field, _) -> field.accessPolicy == AccessPolicy.ALWAYS_READABLE }

    fun primaryConstructorInvariants(returnVariable: VariableEmbedding) =
        readonlyPrimaryConstructorFieldsWithParams().map { (field, variable) ->
            EqCmp(FieldAccess(returnVariable, field), Old(variable))
        }
}

fun FullNamedFunctionSignature.toViperMethod(
    body: Stmt.Seqn?,
    returnVariable: VariableEmbedding,
) = UserMethod(
    name,
    formalArgs.map { it.toLocalVarDecl() },
    returnVariable.toLocalVarDecl(),
    getPreconditions(returnVariable).pureToViper(),
    getPostconditions(returnVariable).pureToViper(),
    body,
    declarationSource.asPosition,
)