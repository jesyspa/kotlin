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

/**
 * Base class for implementations of `PartiallySpecialKotlinFunction`s.
 */
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
        _baseEmbedding = embedding
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
    val byName: Map<MangledName, FunctionEmbedding> = listOf(StringPlusAnyFunction).associateBy { it.embedName() }
}

