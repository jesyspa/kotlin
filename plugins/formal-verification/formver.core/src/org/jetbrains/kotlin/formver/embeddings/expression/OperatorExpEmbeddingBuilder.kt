/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.expression

import org.jetbrains.kotlin.formver.domains.FunctionBuilder
import org.jetbrains.kotlin.formver.domains.InjectionImageFunction
import org.jetbrains.kotlin.formver.embeddings.types.*
import org.jetbrains.kotlin.formver.viper.ast.*


class OperatorExpEmbeddingBuilder {
    private var runtimeTypeFunctionName: String? = null
    private var viperApplicable: Applicable? = null
    private var callableType: FunctionTypeEmbedding? = null
    private var additionalConditions: (FunctionBuilder.() -> Unit)? = null

    fun complete(): OperatorExpEmbeddingTemplate {
        val callableType = callableType ?: error("Signature not specified to buildExpEmbedding.")
        val argumentTypes = callableType.formalArgTypes
        val returnType = callableType.returnType
        check(argumentTypes.size == 1 || argumentTypes.size == 2) { "Operator should take exactly two arguments." }
        val refsOperation = InjectionImageFunction(
            runtimeTypeFunctionName ?: error("No name specified for the underlying viper function."),
            viperApplicable ?: error("No viper operator specified to build ExpEmbedding."),
            argumentTypes.map { it.injection },
            returnType.injection,
            additionalConditions ?: { }
        )
        return OperatorExpEmbeddingTemplate(returnType, refsOperation)
    }

    fun setName(name: String) {
        check(runtimeTypeFunctionName == null) { "Name for underlying viper function is already set." }
        runtimeTypeFunctionName = name
    }

    fun setSignature(signature: FunctionTypeEmbedding) {
        check(callableType == null) { "Signature to build ExpEmbedding is already set." }
        callableType = signature
    }

    fun withSignature(block: FunctionPretypeBuilder.() -> Unit) {
        setSignature(buildFunctionPretype(block))
    }

    data class ViperCallData(
        val args: List<Exp>,
        val pos: Position,
        val info: Info,
        val trafos: Trafos,
    )

    fun viperImplementation(block: ViperCallData.() -> Exp) {
        check(viperApplicable == null) { "Viper implementation for OperatorExpEmbedding is already set." }
        viperApplicable = object : Applicable {
            override fun toFuncApp(args: List<Exp>, pos: Position, info: Info, trafos: Trafos): Exp {
                return ViperCallData(args, pos, info, trafos).block()
            }
        }
    }

    fun additionalConditions(block: FunctionBuilder.() -> Unit) {
        additionalConditions = block
    }
}

fun buildOperatorExpEmbedding(block: OperatorExpEmbeddingBuilder.() -> Unit) =
    OperatorExpEmbeddingBuilder().apply(block).complete()

