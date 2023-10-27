/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.expression

import org.jetbrains.kotlin.formver.asPosition
import org.jetbrains.kotlin.formver.domains.UnitDomain
import org.jetbrains.kotlin.formver.embeddings.BooleanTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.NothingTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.TypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.UnitTypeEmbedding
import org.jetbrains.kotlin.formver.linearization.LinearizationContext
import org.jetbrains.kotlin.formver.linearization.addLabel
import org.jetbrains.kotlin.formver.linearization.pureToViper
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.Label
import org.jetbrains.kotlin.formver.viper.ast.Stmt

data class Block(val exps: List<ExpEmbedding>) : OptionalResultExpEmbedding {
    constructor (vararg exps: ExpEmbedding) : this(exps.toList())

    override val type: TypeEmbedding = exps.lastOrNull()?.type ?: UnitTypeEmbedding

    override fun toViperMaybeStoringIn(result: Exp?, ctx: LinearizationContext) {
        if (exps.isEmpty()) return

        for (exp in exps.take(exps.size - 1)) {
            exp.toViper(ctx)
        }
        exps.last().toViperMaybeStoringIn(result, ctx)
    }
}

data class If(val condition: ExpEmbedding, val thenBranch: ExpEmbedding, val elseBranch: ExpEmbedding, override val type: TypeEmbedding) :
    OptionalResultExpEmbedding {
    override fun toViperMaybeStoringIn(result: Exp?, ctx: LinearizationContext) {
        val condViper = condition.toViper(ctx)
        val thenViper = ctx.asBlock { thenBranch.toViperMaybeStoringIn(result, this) }
        val elseViper = ctx.asBlock { elseBranch.toViperMaybeStoringIn(result, this) }
        ctx.addStatement(Stmt.If(condViper, thenViper, elseViper, ctx.source.asPosition))
    }
}

data class While(
    val condition: ExpEmbedding,
    val body: ExpEmbedding,
    val breakLabel: Label,
    val continueLabel: Label,
    val invariants: List<ExpEmbedding>,
) : DirectResultExpEmbedding {
    override val type: TypeEmbedding = UnitTypeEmbedding

    override fun toViper(ctx: LinearizationContext): Exp {
        val condVar = ctx.freshAnonVar(BooleanTypeEmbedding)
        ctx.addLabel(continueLabel)
        condition.toViperStoringIn(condVar, ctx)
        val bodyBlock = ctx.asBlock {
            body.toViper(this)
            condition.toViperStoringIn(condVar, this)
        }
        ctx.addStatement(
            Stmt.While(
                condVar,
                invariants.pureToViper(ctx.source),
                bodyBlock,
                ctx.source.asPosition
            )
        )
        return UnitDomain.element
    }
}

data class TryCatch(val todo: ExpEmbedding) : StoredResultExpEmbedding {
    override val type: TypeEmbedding
        get() = TODO("Not yet implemented")

    override fun toViperStoringIn(result: Exp, ctx: LinearizationContext) {
        TODO("Not yet implemented")
    }
}

data class Return(val returnVariable: VariableEmbedding, val returnTarget: Label, val returnValue: ExpEmbedding) : NoResultExpEmbedding {
    override fun toViperUnusedResult(ctx: LinearizationContext) {
        returnValue.toViperStoringIn(returnVariable.toLocalVarUse(), ctx)
        ctx.addStatement(returnTarget.toGoto(ctx.source.asPosition))
    }
}

data class Goto(val target: Label) : NoResultExpEmbedding {
    override val type: TypeEmbedding = NothingTypeEmbedding
    override fun toViperUnusedResult(ctx: LinearizationContext) {
        ctx.addStatement(target.toGoto(ctx.source.asPosition))
    }
}

