/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.expression

import org.jetbrains.kotlin.formver.asPosition
import org.jetbrains.kotlin.formver.domains.RuntimeTypeDomain
import org.jetbrains.kotlin.formver.embeddings.IntTypeEmbedding
import org.jetbrains.kotlin.formver.linearization.LinearizationContext

sealed interface IntArithmeticExpression : BinaryDirectResultExpEmbedding {
    override val type
        get() = IntTypeEmbedding
}

data class Add(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntArithmeticExpression {
    override fun toViper(ctx: LinearizationContext) =
        RuntimeTypeDomain.plusInts(left.toViper(ctx), right.toViper(ctx), pos = ctx.source.asPosition)
}

data class Sub(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntArithmeticExpression {
    override fun toViper(ctx: LinearizationContext) =
        RuntimeTypeDomain.minusInts(left.toViper(ctx), right.toViper(ctx), pos = ctx.source.asPosition)
}

data class Mul(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntArithmeticExpression {
    override fun toViper(ctx: LinearizationContext) =
        RuntimeTypeDomain.timesInts(left.toViper(ctx), right.toViper(ctx), pos = ctx.source.asPosition)
}

// TODO: handle separately, inhale rhs != 0
data class Div(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntArithmeticExpression {
    override fun toViper(ctx: LinearizationContext) =
        RuntimeTypeDomain.divInts(left.toViper(ctx), right.toViper(ctx), pos = ctx.source.asPosition)
}

data class Mod(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntArithmeticExpression {
    override fun toViper(ctx: LinearizationContext) =
        RuntimeTypeDomain.remInts(left.toViper(ctx), right.toViper(ctx), pos = ctx.source.asPosition)
}
