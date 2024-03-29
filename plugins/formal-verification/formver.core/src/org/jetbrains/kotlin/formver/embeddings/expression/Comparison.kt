/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.expression

import org.jetbrains.kotlin.formver.asPosition
import org.jetbrains.kotlin.formver.domains.InjectionImageFunction
import org.jetbrains.kotlin.formver.domains.RuntimeTypeDomain
import org.jetbrains.kotlin.formver.embeddings.BooleanTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.SourceRole
import org.jetbrains.kotlin.formver.embeddings.asInfo
import org.jetbrains.kotlin.formver.linearization.LinearizationContext
import org.jetbrains.kotlin.formver.viper.ast.EqAny
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.NeAny
import org.jetbrains.kotlin.formver.viper.ast.OperatorFunction

sealed interface AnyComparisonExpression : BinaryDirectResultExpEmbedding {
    override val type
        get() = BooleanTypeEmbedding

    val comparisonOperation: OperatorFunction

    override fun toViper(ctx: LinearizationContext): Exp =
        RuntimeTypeDomain.boolInjection.toRef(
            comparisonOperation(
                left.toViper(ctx),
                right.toViper(ctx),
                pos = ctx.source.asPosition,
                info = sourceRole.asInfo
            )
        )

    // The first option is included to avoid complex comparisons in form `intToRef(0) == intToRef(1)`.
    // However, when comparing `intToRef(3) == nullValue()` we don't have other options since `toViperBuiltinType` will
    // return different types.
    override fun toViperBuiltinType(ctx: LinearizationContext): Exp =
        if (left.type == right.type)
            comparisonOperation(
                left.toViperBuiltinType(ctx),
                right.toViperBuiltinType(ctx),
                pos = ctx.source.asPosition,
                info = sourceRole.asInfo
            )
        else
            comparisonOperation(
                left.toViper(ctx),
                right.toViper(ctx),
                pos = ctx.source.asPosition,
                info = sourceRole.asInfo
            )
}

sealed interface IntComparisonExpression : OperationBaseExpEmbedding {
    override val type
        get() = BooleanTypeEmbedding
}

data class LtCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : IntComparisonExpression {
    override val objectsOperation: InjectionImageFunction = RuntimeTypeDomain.ltInts
}

data class LeCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : IntComparisonExpression {
    override val objectsOperation: InjectionImageFunction = RuntimeTypeDomain.leInts
}

data class GtCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : IntComparisonExpression {
    override val objectsOperation: InjectionImageFunction = RuntimeTypeDomain.gtInts
}

data class GeCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : IntComparisonExpression {
    override val objectsOperation: InjectionImageFunction = RuntimeTypeDomain.geInts
}

data class EqCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : AnyComparisonExpression {

    override val comparisonOperation = EqAny
}

data class NeCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : AnyComparisonExpression {

    override val comparisonOperation = NeAny
}

fun ExpEmbedding.notNullCmp(): ExpEmbedding = NeCmp(this, NullLit)

