/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.formver.asPosition
import org.jetbrains.kotlin.formver.domains.*
import org.jetbrains.kotlin.formver.embeddings.callables.DuplicableFunction
import org.jetbrains.kotlin.formver.linearization.LinearizationContext
import org.jetbrains.kotlin.formver.linearization.addLabel
import org.jetbrains.kotlin.formver.linearization.toPureViperExps
import org.jetbrains.kotlin.formver.linearization.withNewVar
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.Label
import org.jetbrains.kotlin.formver.viper.ast.Stmt

sealed interface ExpEmbedding {
    val type: TypeEmbedding

    fun toViperExp(ctx: LinearizationContext): Exp
    fun toViperStoringIn(result: Exp, ctx: LinearizationContext)
    fun ignoringCasts(): ExpEmbedding = this
}

fun ExpEmbedding.withType(newType: TypeEmbedding): ExpEmbedding =
    if (newType == type) this else Cast(this, newType)

fun ExpEmbedding.withPosition(source: KtSourceElement?): ExpEmbedding =
    source?.let { WithPosition(this, source) } ?: this

fun List<ExpEmbedding>.toViperExps(ctx: LinearizationContext): List<Exp> = map { it.toViperExp(ctx) }

fun List<ExpEmbedding>.toConjunction(): ExpEmbedding =
    if (isEmpty()) BooleanLit(true)
    else reduce { l, r -> And(l, r) }

sealed interface DirectResultExpEmbedding : ExpEmbedding {
    override fun toViperStoringIn(result: Exp, ctx: LinearizationContext) {
        ctx.addStatement(Stmt.assign(result, toViperExp(ctx)))
    }
}

sealed interface NestedResultExpEmbedding : ExpEmbedding {
    override fun toViperExp(ctx: LinearizationContext): Exp =
        ctx.withNewVar(type) { v ->
            toViperStoringIn(v.toViperExp(ctx), ctx)
            UnitDomain.element
        }
}

// For nodes that don't evaluate to a value, e.g. `Return`, `Goto`
sealed interface NoResultExpEmbedding : ExpEmbedding {
    override val type: TypeEmbedding
        get() = NothingTypeEmbedding

    // Result ignored, since it is never used.
    override fun toViperStoringIn(result: Exp, ctx: LinearizationContext) {
        toNoReturnViperExp(ctx)
    }

    override fun toViperExp(ctx: LinearizationContext): Exp {
        toNoReturnViperExp(ctx)
        return UnitDomain.element
    }

    fun toNoReturnViperExp(ctx: LinearizationContext)
}

sealed interface PassthroughExpEmbedding : ExpEmbedding {
    val inner: ExpEmbedding
    override val type: TypeEmbedding
        get() = inner.type

    override fun toViperExp(ctx: LinearizationContext): Exp =
        withPassthroughHook(ctx) { inner.toViperExp(this) }


    override fun toViperStoringIn(result: Exp, ctx: LinearizationContext) {
        withPassthroughHook(ctx) { inner.toViperStoringIn(result, ctx) }
    }

    fun <R> withPassthroughHook(ctx: LinearizationContext, action: LinearizationContext.() -> R): R
}

/**
 * Note: this interface cannot be used for nodes with children, since those children may not themselves be pure.
 */
sealed interface PureExpEmbedding : DirectResultExpEmbedding {
    fun toViper(source: KtSourceElement? = null): Exp

    override fun toViperExp(ctx: LinearizationContext): Exp = toViper(ctx.source)
}

sealed interface IntArithmeticExpression : DirectResultExpEmbedding {
    val left: ExpEmbedding
    val right: ExpEmbedding
    override val type
        get() = IntTypeEmbedding
}

data class Add(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntArithmeticExpression {
    override fun toViperExp(ctx: LinearizationContext) = Exp.Add(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}

data class Sub(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntArithmeticExpression {
    override fun toViperExp(ctx: LinearizationContext) = Exp.Sub(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}

data class Mul(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntArithmeticExpression {
    override fun toViperExp(ctx: LinearizationContext) = Exp.Mul(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}

data class Div(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntArithmeticExpression {
    // TODO: add inhale for rhs != 0
    override fun toViperExp(ctx: LinearizationContext) = Exp.Div(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}

data class Mod(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntArithmeticExpression {
    // TODO: add inhale for rhs != 0
    override fun toViperExp(ctx: LinearizationContext) = Exp.Mod(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}


sealed interface IntComparisonExpression : DirectResultExpEmbedding {
    val left: ExpEmbedding
    val right: ExpEmbedding
    override val type
        get() = BooleanTypeEmbedding
}

data class LtCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntComparisonExpression {
    override fun toViperExp(ctx: LinearizationContext) = Exp.LtCmp(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}

data class LeCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntComparisonExpression {
    override fun toViperExp(ctx: LinearizationContext) = Exp.LeCmp(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}

data class GtCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntComparisonExpression {
    override fun toViperExp(ctx: LinearizationContext) = Exp.GtCmp(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}

data class GeCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : IntComparisonExpression {
    override fun toViperExp(ctx: LinearizationContext) = Exp.GeCmp(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}


data class EqCmp(
    val left: ExpEmbedding,
    val right: ExpEmbedding,
) : DirectResultExpEmbedding {
    override val type = BooleanTypeEmbedding

    override fun toViperExp(ctx: LinearizationContext) = Exp.EqCmp(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}

data class NeCmp(
    val left: ExpEmbedding,
    val right: ExpEmbedding,
) : DirectResultExpEmbedding {
    override val type = BooleanTypeEmbedding

    override fun toViperExp(ctx: LinearizationContext) = Exp.NeCmp(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}


sealed interface BinaryBooleanExpression : DirectResultExpEmbedding {
    val left: ExpEmbedding
    val right: ExpEmbedding
    override val type: BooleanTypeEmbedding
        get() = BooleanTypeEmbedding
}

data class And(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : BinaryBooleanExpression {
    override fun toViperExp(ctx: LinearizationContext) = Exp.And(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}

data class Or(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : BinaryBooleanExpression {
    override fun toViperExp(ctx: LinearizationContext) = Exp.Or(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}

data class Implies(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
) : BinaryBooleanExpression {
    override fun toViperExp(ctx: LinearizationContext) = Exp.Implies(left.toViperExp(ctx), right.toViperExp(ctx), ctx.source.asPosition)
}

data class Not(
    val exp: ExpEmbedding,
) : DirectResultExpEmbedding {
    override val type = BooleanTypeEmbedding
    override fun toViperExp(ctx: LinearizationContext) = Exp.Not(exp.toViperExp(ctx), ctx.source.asPosition)
}

data class Old(
    val exp: ExpEmbedding,
) : DirectResultExpEmbedding {
    override val type: TypeEmbedding = exp.type
    override fun toViperExp(ctx: LinearizationContext): Exp = Exp.Old(exp.toViperExp(ctx), ctx.source.asPosition)
}

data object UnitLit : PureExpEmbedding {
    override val type = UnitTypeEmbedding
    override fun toViper(source: KtSourceElement?): Exp = UnitDomain.element
}

data class IntLit(val value: Int) : PureExpEmbedding {
    override val type = IntTypeEmbedding
    override fun toViper(source: KtSourceElement?): Exp = Exp.IntLit(value, source.asPosition)
}

data class BooleanLit(val value: Boolean) : PureExpEmbedding {
    override val type = BooleanTypeEmbedding
    override fun toViper(source: KtSourceElement?) = Exp.BoolLit(value, source.asPosition)
}

data class NullLit(val elemType: TypeEmbedding) : PureExpEmbedding {
    override val type = NullableTypeEmbedding(elemType)
    override fun toViper(source: KtSourceElement?) = NullableDomain.nullVal(elemType.viperType, source)
}

data class Is(val exp: ExpEmbedding, val comparisonType: TypeEmbedding) : DirectResultExpEmbedding {
    override val type = BooleanTypeEmbedding

    override fun toViperExp(ctx: LinearizationContext) =
        TypeDomain.isSubtype(TypeOfDomain.typeOf(exp.toViperExp(ctx)), comparisonType.runtimeType, pos = ctx.source.asPosition)
}

data class Cast(val exp: ExpEmbedding, override val type: TypeEmbedding) : DirectResultExpEmbedding {
    override fun toViperExp(ctx: LinearizationContext) = CastingDomain.cast(exp.toViperExp(ctx), type, ctx.source)
    override fun ignoringCasts(): ExpEmbedding = exp
}

data class As(val exp: ExpEmbedding, override val type: TypeEmbedding) : NestedResultExpEmbedding {
    override fun toViperStoringIn(result: Exp, ctx: LinearizationContext) {
        TODO("Not yet implemented")
    }
}

data class SafeAs(val exp: ExpEmbedding, override val type: TypeEmbedding) : DirectResultExpEmbedding {
    override fun toViperExp(ctx: LinearizationContext) = TODO()
}

data class FieldAccess(val receiver: ExpEmbedding, val field: FieldEmbedding) : DirectResultExpEmbedding {
    override val type: TypeEmbedding = field.type
    override fun toViperExp(ctx: LinearizationContext) = Exp.FieldAccess(receiver.toViperExp(ctx), field.toViper(), ctx.source.asPosition)
}

data class DuplicableCall(val exp: ExpEmbedding) : DirectResultExpEmbedding {
    override val type: TypeEmbedding = BooleanTypeEmbedding
    override fun toViperExp(ctx: LinearizationContext): Exp =
        DuplicableFunction.toFuncApp(listOf(exp.toViperExp(ctx)), ctx.source.asPosition)
}

data class TypeOfCall(val exp: ExpEmbedding) : DirectResultExpEmbedding {
    override val type: TypeEmbedding = TypeInfoTypeEmbedding
    override fun toViperExp(ctx: LinearizationContext): Exp = TypeOfDomain.typeOf(exp.toViperExp(ctx), ctx.source.asPosition)
}

/**
 * Especially when working with type information, there are a number of expressions that do not have a corresponding `ExpEmbedding`.
 * We will eventually want to solve this somehow, but there are still open design questions there, so for now this wrapper will
 * do the job.
 */
data class ExpWrapper(val value: Exp, override val type: TypeEmbedding) : PureExpEmbedding {
    override fun toViper(source: KtSourceElement?): Exp = value
}

data class Assign(val lhs: LValueEmbedding, val rhs: ExpEmbedding) : NestedResultExpEmbedding {
    override val type: TypeEmbedding
        get() = TODO("Not yet implemented")

    override fun toViperStoringIn(result: Exp, ctx: LinearizationContext) {
        TODO("Not yet implemented")
    }
}

data class If(val condition: ExpEmbedding, val thenBranch: ExpEmbedding, val elseBranch: ExpEmbedding, override val type: TypeEmbedding) :
    NestedResultExpEmbedding {
    override fun toViperStoringIn(result: Exp, ctx: LinearizationContext) {
        TODO("Not yet implemented")
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

    override fun toViperExp(ctx: LinearizationContext): Exp {
        val condVar = ctx.newVar(BooleanTypeEmbedding)
        ctx.addLabel(continueLabel)
        condition.toViperStoringIn(condVar.toLocalVarUse(), ctx)
        val bodyBlock = ctx.withNewScopeToBlock {
            body.toViperExp(this)
            condition.toViperStoringIn(condVar.toLocalVarUse(), this)
        }
        ctx.addStatement(
            Stmt.While(
                condVar.toLocalVarUse(),
                invariants.toPureViperExps(ctx.source),
                bodyBlock,
                ctx.source.asPosition
            )
        )
        return UnitDomain.element
    }
}

data class TryCatch(val todo: ExpEmbedding) : NestedResultExpEmbedding {
    override val type: TypeEmbedding
        get() = TODO("Not yet implemented")

    override fun toViperStoringIn(result: Exp, ctx: LinearizationContext) {
        TODO("Not yet implemented")
    }
}

data class Return(val returnVariable: VariableEmbedding, val returnTarget: Label, val returnValue: ExpEmbedding) : NoResultExpEmbedding {
    override fun toNoReturnViperExp(ctx: LinearizationContext) {
        returnValue.toViperStoringIn(returnVariable.toLocalVarUse(), ctx)
        ctx.addStatement(returnTarget.toGoto(ctx.source.asPosition))
    }
}

data class Goto(val target: Label) : NoResultExpEmbedding {
    override val type: TypeEmbedding = NothingTypeEmbedding
    override fun toNoReturnViperExp(ctx: LinearizationContext) {
        ctx.addStatement(target.toGoto(ctx.source.asPosition))
    }
}

data class WithPosition(override val inner: ExpEmbedding, val source: KtSourceElement) : PassthroughExpEmbedding {
    override fun <R> withPassthroughHook(ctx: LinearizationContext, action: LinearizationContext.() -> R): R =
        ctx.withPosition(source, action)
}

data class Block(val exps: List<ExpEmbedding>) : DirectResultExpEmbedding {
    constructor (vararg exps: ExpEmbedding) : this(exps.toList())

    override val type: TypeEmbedding = exps.lastOrNull()?.type ?: UnitTypeEmbedding
    override fun toViperExp(ctx: LinearizationContext): Exp =
        exps.fold(UnitDomain.element as Exp) { _, elem -> elem.toViperExp(ctx) }
}

data class Scope(override val inner: ExpEmbedding) : PassthroughExpEmbedding {
    override fun <R> withPassthroughHook(ctx: LinearizationContext, action: LinearizationContext.() -> R): R =
        ctx.withNewScope(action)
}

data object ErrorExp : NoResultExpEmbedding {
    override val type: TypeEmbedding = NothingTypeEmbedding
    override fun toNoReturnViperExp(ctx: LinearizationContext) {
        ctx.addImmediateStatement(Stmt.Inhale(Exp.BoolLit(false)))
    }
}