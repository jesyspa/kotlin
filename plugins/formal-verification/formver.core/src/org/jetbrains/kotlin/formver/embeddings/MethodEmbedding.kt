/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.formver.conversion.ResultTrackingContext
import org.jetbrains.kotlin.formver.conversion.StmtConversionContext
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.Method

interface MethodEmbedding : MethodSignatureEmbedding {
    val preconditions: List<Exp>
    val postconditions: List<Exp>

    val viperMethod: Method?

    fun insertCall(argsFir: List<FirExpression>, ctx: StmtConversionContext<ResultTrackingContext>): Exp
}
