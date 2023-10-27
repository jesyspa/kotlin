/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.expression

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.formver.embeddings.NothingTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.TypeEmbedding
import org.jetbrains.kotlin.formver.linearization.LinearizationContext
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.Stmt

/**
 * Especially when working with type information, there are a number of expressions that do not have a corresponding `ExpEmbedding`.
 * We will eventually want to solve this somehow, but there are still open design questions there, so for now this wrapper will
 * do the job.
 */
data class ExpWrapper(val value: Exp, override val type: TypeEmbedding) : PureExpEmbedding {
    override fun toViper(source: KtSourceElement?): Exp = value
}

data object ErrorExp : NoResultExpEmbedding {
    override val type: TypeEmbedding = NothingTypeEmbedding
    override fun toViperUnusedResult(ctx: LinearizationContext) {
        ctx.addStatement(Stmt.Inhale(Exp.BoolLit(false)))
    }
}
