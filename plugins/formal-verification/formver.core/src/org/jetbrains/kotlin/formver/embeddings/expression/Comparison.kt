/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.expression

import org.jetbrains.kotlin.formver.asPosition
import org.jetbrains.kotlin.formver.domains.RuntimeTypeDomain
import org.jetbrains.kotlin.formver.embeddings.SimpleBooleanTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.SourceRole
import org.jetbrains.kotlin.formver.embeddings.asInfo
import org.jetbrains.kotlin.formver.linearization.LinearizationContext
import org.jetbrains.kotlin.formver.viper.ast.Exp

sealed interface ComparisonExpression : BinaryDirectResultExpEmbedding {
    override val type
        get() = SimpleBooleanTypeEmbedding
}

data class LtCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : ComparisonExpression {
    override fun toViper(ctx: LinearizationContext) =
        RuntimeTypeDomain.ltInts(left.toViper(ctx), right.toViper(ctx), pos = ctx.source.asPosition, info = sourceRole.asInfo)
}

data class LeCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : ComparisonExpression {
    override fun toViper(ctx: LinearizationContext) =
        RuntimeTypeDomain.leInts(left.toViper(ctx), right.toViper(ctx), pos = ctx.source.asPosition, info = sourceRole.asInfo)
}

data class GtCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : ComparisonExpression {
    override fun toViper(ctx: LinearizationContext) =
        RuntimeTypeDomain.gtInts(left.toViper(ctx), right.toViper(ctx), pos = ctx.source.asPosition, info = sourceRole.asInfo)
}

data class GeCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : ComparisonExpression {
    override fun toViper(ctx: LinearizationContext) =
        RuntimeTypeDomain.geInts(left.toViper(ctx), right.toViper(ctx), pos = ctx.source.asPosition, info = sourceRole.asInfo)
}

data class EqCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : ComparisonExpression {
    override fun toViper(ctx: LinearizationContext) =
        RuntimeTypeDomain.boolInjection.toRef(
            Exp.EqCmp(
                left.toViper(ctx),
                right.toViper(ctx),
                pos = ctx.source.asPosition,
                info = sourceRole.asInfo
            )
        )
}

data class NeCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : ComparisonExpression {
    override fun toViper(ctx: LinearizationContext) =
        RuntimeTypeDomain.boolInjection.toRef(
            Exp.NeCmp(
                left.toViper(ctx),
                right.toViper(ctx),
                pos = ctx.source.asPosition,
                info = sourceRole.asInfo
            )
        )
}

fun ExpEmbedding.notNullCmp(): ExpEmbedding = NeCmp(this, NullLit)

