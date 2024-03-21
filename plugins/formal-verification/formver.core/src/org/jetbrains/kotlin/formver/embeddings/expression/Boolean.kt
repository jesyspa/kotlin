/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.expression

import org.jetbrains.kotlin.formver.asPosition
import org.jetbrains.kotlin.formver.domains.InjectionImageFunction
import org.jetbrains.kotlin.formver.domains.RuntimeTypeDomain
import org.jetbrains.kotlin.formver.domains.toViperCondition
import org.jetbrains.kotlin.formver.embeddings.*
import org.jetbrains.kotlin.formver.linearization.LinearizationContext
import org.jetbrains.kotlin.formver.viper.ast.BuiltinFunction
import org.jetbrains.kotlin.formver.viper.ast.*

sealed interface BinaryBooleanExpression : BinaryDirectResultExpEmbedding {
    override val type: BooleanTypeEmbedding
        get() = when {
            left.type is PermissionsContainingBooleanTypeEmbedding -> PermissionsContainingBooleanTypeEmbedding
            right.type is PermissionsContainingBooleanTypeEmbedding -> PermissionsContainingBooleanTypeEmbedding
            else -> SimpleBooleanTypeEmbedding
        }

    val refOp: InjectionImageFunction

    private fun argToViper(arg: ExpEmbedding, ctx: LinearizationContext) = arg.toViper(ctx).run {
        if (this@BinaryBooleanExpression.type is PermissionsContainingBooleanTypeEmbedding && arg.type is SimpleBooleanTypeEmbedding) {
            this.toViperCondition()
        } else {
            this
        }
    }

    override fun toViper(ctx: LinearizationContext): Exp {
        val leftExp = argToViper(left, ctx)
        val rightExp = argToViper(right, ctx)
        val appliedOp = if (type is PermissionsContainingBooleanTypeEmbedding) {
            refOp.original
        } else {
            refOp
        }
        return appliedOp(leftExp, rightExp, pos = ctx.source.asPosition, info = sourceRole.asInfo)
    }
}

data class And(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : BinaryBooleanExpression {
    override val refOp
        get() = RuntimeTypeDomain.andBools
}

data class Or(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : BinaryBooleanExpression {
    override val refOp
        get() = RuntimeTypeDomain.orBools
}

data class Implies(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val sourceRole: SourceRole? = null,
) : BinaryBooleanExpression {
    override val refOp
        get() = RuntimeTypeDomain.impliesBools
}

data class Not(
    override val inner: ExpEmbedding,
    override val sourceRole: SourceRole? = null
) : UnaryDirectResultExpEmbedding {
    override val type = SimpleBooleanTypeEmbedding
    override fun toViper(ctx: LinearizationContext) =
        RuntimeTypeDomain.notBool(inner.toViper(ctx), pos = ctx.source.asPosition, info = sourceRole.asInfo)
}

fun List<ExpEmbedding>.toConjunction(): ExpEmbedding =
    if (isEmpty()) BooleanLit(true)
    else reduce { l, r -> And(l, r) }
