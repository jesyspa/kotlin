/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.callables

import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.formver.conversion.ResultTrackingContext
import org.jetbrains.kotlin.formver.conversion.StmtConversionContext
import org.jetbrains.kotlin.formver.embeddings.ExpEmbedding

interface CallableEmbedding : CallableSignature {
    fun insertCallImpl(args: List<ExpEmbedding>, ctx: StmtConversionContext<ResultTrackingContext>): ExpEmbedding

    // Some callables can *only* be called via the FIR.
    fun insertFirCallImpl(firArgs: List<FirExpression>, ctx: StmtConversionContext<ResultTrackingContext>): ExpEmbedding =
        insertCall(firArgs.map { ctx.convert(it) }, ctx)
}

fun CallableEmbedding.insertCall(args: List<ExpEmbedding>, ctx: StmtConversionContext<ResultTrackingContext>): ExpEmbedding =
    callWithTypecasts(args) {
        insertCallImpl(it, ctx)
    }