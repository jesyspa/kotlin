/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.expression

import org.jetbrains.kotlin.formver.embeddings.expression.OperatorExpEmbeddings.And
import org.jetbrains.kotlin.formver.embeddings.expression.OperatorExpEmbeddings.Or
import org.jetbrains.kotlin.formver.embeddings.types.buildType
import org.jetbrains.kotlin.formver.linearization.LinearizationContext
import org.jetbrains.kotlin.formver.linearization.LogicOperatorPolicy
import org.jetbrains.kotlin.formver.viper.ast.Exp

interface SequentialLogicOperatorEmbedding : BinaryDirectResultExpEmbedding {
    override val type
        get() = buildType { boolean() }
}

class SequentialAnd(override val left: ExpEmbedding, override val right: ExpEmbedding) : SequentialLogicOperatorEmbedding {
    override fun toViper(ctx: LinearizationContext): Exp =
        when (ctx.logicOperatorPolicy) {
            LogicOperatorPolicy.CONVERT_TO_IF -> If(left, right, BooleanLit(false), buildType { boolean() })
            LogicOperatorPolicy.CONVERT_TO_EXPRESSION -> And(left, right)
        }.toViper(ctx)
}

class SequentialOr(override val left: ExpEmbedding, override val right: ExpEmbedding) : SequentialLogicOperatorEmbedding {
    override fun toViper(ctx: LinearizationContext): Exp =
        when (ctx.logicOperatorPolicy) {
            LogicOperatorPolicy.CONVERT_TO_IF -> If(left, BooleanLit(true), right, buildType { boolean() })
            LogicOperatorPolicy.CONVERT_TO_EXPRESSION -> Or(left, right)
        }.toViper(ctx)
}