/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.callables

import org.jetbrains.kotlin.formver.conversion.StmtConversionContext
import org.jetbrains.kotlin.formver.embeddings.expression.ExpEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.OperatorExpEmbeddings.AddStringString
import org.jetbrains.kotlin.formver.embeddings.types.buildFunctionPretype
import org.jetbrains.kotlin.formver.embeddings.types.equalToType
import org.jetbrains.kotlin.formver.embeddings.types.nullableAny
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.Method

/**
 * Functions that should sometimes be handled separately by our conversion depending on arguments they're called with.
 */
interface PartiallySpecialKotlinFunction : SeparatelyHandledKotlinFunction {
    val baseEmbedding: FunctionEmbedding?
    fun tryInsertCall(args: List<ExpEmbedding>, ctx: StmtConversionContext): ExpEmbedding?
    override fun insertCall(args: List<ExpEmbedding>, ctx: StmtConversionContext): ExpEmbedding {
        return tryInsertCall(args, ctx) ?: baseEmbedding?.insertCall(args, ctx)
        ?: error("Base embedding for partially special function $name not specified")
    }

    fun initBaseEmbedding(embedding: FunctionEmbedding)

    override val viperMethod: Method?
        get() = baseEmbedding?.viperMethod
}

abstract class AbstractPartiallySpecialKotlinFunction(
    vararg packageName: String,
    override val className: String? = null,
    override val name: String,
) : PartiallySpecialKotlinFunction {
    private var _baseEmbedding: FunctionEmbedding? = null
    override val baseEmbedding
        get() = _baseEmbedding

    override val packageName = packageName.toList()

    override fun initBaseEmbedding(embedding: FunctionEmbedding) {
        check(_baseEmbedding == null) { "Base embedding for partially special function $name already initialized." }
        _baseEmbedding = baseEmbedding
    }
}

data object StringPlusAnyFunction : AbstractPartiallySpecialKotlinFunction("kotlin", className = "String", name = "plus") {
    override fun tryInsertCall(args: List<ExpEmbedding>, ctx: StmtConversionContext): ExpEmbedding? =
        if (args[1].type.equalToType { string() }) AddStringString(args[0], args[1]) else null

    override val callableType = buildFunctionPretype {
        withDispatchReceiver { string() }
        withParam { nullableAny() }
        withReturnType { string() }
    }
}

object PartiallySpecialKotlinFunctions {
    val byName: Map<MangledName, PartiallySpecialKotlinFunction> = listOf(StringPlusAnyFunction).associateBy { it.embedName() }
}

