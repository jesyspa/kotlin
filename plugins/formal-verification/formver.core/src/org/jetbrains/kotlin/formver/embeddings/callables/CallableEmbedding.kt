/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.callables

import org.jetbrains.kotlin.formver.conversion.StmtConversionContext
import org.jetbrains.kotlin.formver.embeddings.FunctionTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.ExpEmbedding

/**
 * Kotlin entity that can be called.
 */
interface CallableEmbedding {
    val type: FunctionTypeEmbedding
    fun insertCall(args: List<ExpEmbedding>, ctx: StmtConversionContext): ExpEmbedding
}
