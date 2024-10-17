/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.callables

import org.jetbrains.kotlin.formver.conversion.StmtConversionContext
import org.jetbrains.kotlin.formver.embeddings.expression.ExpEmbedding
import org.jetbrains.kotlin.formver.embeddings.types.FunctionTypeEmbedding
import org.jetbrains.kotlin.formver.viper.MangledName

class FullySpecialKotlinFunctionBuilder {
    private val byName = mutableMapOf<MangledName, FunctionEmbedding>()

    fun withCallableType(
        callableType: FunctionTypeEmbedding,
        functionsBlock: SpecialKotlinFunctionBuilderWithCallableType.() -> Unit,
    ) {
        val builderWithCallableType = SpecialKotlinFunctionBuilderWithCallableType(callableType)

        builderWithCallableType.functionsBlock()
    }

    fun addFunction(
        callableType: FunctionTypeEmbedding,
        vararg packageName: String,
        className: String? = null,
        name: String,
        body: (List<ExpEmbedding>, StmtConversionContext) -> ExpEmbedding,
    ) {
        withCallableType(callableType) {
            addFunction(*packageName, className = className, name = name, body = body)
        }
    }

    inner class SpecialKotlinFunctionBuilderWithCallableType(private val callableType: FunctionTypeEmbedding) {
        fun addFunction(
            vararg packageName: String,
            className: String? = null,
            name: String,
            body: (List<ExpEmbedding>, StmtConversionContext) -> ExpEmbedding,
        ) {

            val newFunction = object : FullySpecialKotlinFunction {
                override val name: String = name
                override val packageName = packageName.toList()
                override val callableType = this@SpecialKotlinFunctionBuilderWithCallableType.callableType
                override val className = className

                override fun insertCall(
                    args: List<ExpEmbedding>,
                    ctx: StmtConversionContext,
                ): ExpEmbedding = body(args, ctx)
            }
            newFunction.let { byName[it.embedName()] = it }
        }
    }

    fun complete(): Map<MangledName, FunctionEmbedding> = byName
}

fun buildFullySpecialFunctions(functionsBlock: FullySpecialKotlinFunctionBuilder.() -> Unit): Map<MangledName, FunctionEmbedding> =
    FullySpecialKotlinFunctionBuilder().apply(functionsBlock).complete()

