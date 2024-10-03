/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.expression

import org.jetbrains.kotlin.formver.asPosition
import org.jetbrains.kotlin.formver.domains.InjectionImageFunction
import org.jetbrains.kotlin.formver.embeddings.SourceRole
import org.jetbrains.kotlin.formver.embeddings.asInfo
import org.jetbrains.kotlin.formver.embeddings.types.TypeEmbedding
import org.jetbrains.kotlin.formver.linearization.LinearizationContext
import org.jetbrains.kotlin.formver.viper.ast.Exp

interface InjectionBasedExpEmbedding : DirectResultExpEmbedding {
    val refsOperation: InjectionImageFunction
    val builtinsOperation
        get() = refsOperation.original
}

interface BinaryOperatorExpEmbedding : BinaryDirectResultExpEmbedding, InjectionBasedExpEmbedding {

    override fun toViper(ctx: LinearizationContext): Exp {
        return refsOperation(left.toViper(ctx), right.toViper(ctx), pos = ctx.source.asPosition, info = sourceRole.asInfo)
    }

    override fun toViperBuiltinType(ctx: LinearizationContext): Exp {
        return builtinsOperation(
            left.toViperBuiltinType(ctx),
            right.toViperBuiltinType(ctx),
            pos = ctx.source.asPosition,
            info = sourceRole.asInfo
        )
    }
}

interface UnaryOperatorExpEmbedding : UnaryDirectResultExpEmbedding, InjectionBasedExpEmbedding {
    override fun toViper(ctx: LinearizationContext) =
        refsOperation(inner.toViper(ctx), pos = ctx.source.asPosition, info = sourceRole.asInfo)

    override fun toViperBuiltinType(ctx: LinearizationContext) =
        builtinsOperation(inner.toViperBuiltinType(ctx), pos = ctx.source.asPosition, info = sourceRole.asInfo)
}

class OperatorExpEmbeddingTemplate(val type: TypeEmbedding, val refsOperation: InjectionImageFunction) {
    operator fun invoke(left: ExpEmbedding, right: ExpEmbedding, sourceRole: SourceRole? = null): BinaryOperatorExpEmbedding =
        object : BinaryOperatorExpEmbedding {
            override val refsOperation: InjectionImageFunction = this@OperatorExpEmbeddingTemplate.refsOperation
            override val type = this@OperatorExpEmbeddingTemplate.type
            override val left = left
            override val right = right
            override val sourceRole = sourceRole
        }

    operator fun invoke(inner: ExpEmbedding, sourceRole: SourceRole? = null): UnaryOperatorExpEmbedding =
        object : UnaryOperatorExpEmbedding {
            override val refsOperation: InjectionImageFunction = this@OperatorExpEmbeddingTemplate.refsOperation
            override val type = this@OperatorExpEmbeddingTemplate.type
            override val inner = inner
            override val sourceRole = sourceRole
        }
}

fun List<ExpEmbedding>.toConjunction(): ExpEmbedding =
    if (isEmpty()) BooleanLit(true)
    else reduce { l, r -> OperatorExpEmbeddings.And(l, r) }
