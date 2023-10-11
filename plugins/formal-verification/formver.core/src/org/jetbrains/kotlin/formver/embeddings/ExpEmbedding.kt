/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.formver.domains.*
import org.jetbrains.kotlin.formver.viper.ast.AccessPredicate
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.PermExp
import org.jetbrains.kotlin.formver.viper.ast.Position

sealed interface ExpEmbedding {
    val type: TypeEmbedding
    val pos: KtSourceElement?

    fun toViper(): Exp
    fun ignoringCasts(): ExpEmbedding = this

    fun withType(newType: TypeEmbedding): ExpEmbedding =
        if (newType == type) this else Cast(this, newType, pos)
}

fun List<ExpEmbedding>.toViper(): List<Exp> = map { it.toViper() }

sealed interface IntArithExpression : ExpEmbedding {
    val left: ExpEmbedding
    val right: ExpEmbedding
    override val type
        get() = IntTypeEmbedding
}

data class Add(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : IntArithExpression {
    override fun toViper() = Exp.Add(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}

data class Sub(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : IntArithExpression {
    override fun toViper() = Exp.Sub(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}

data class Mul(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : IntArithExpression {
    override fun toViper() = Exp.Mul(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}

data class Div(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : IntArithExpression {
    override fun toViper() = Exp.Div(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}

data class Mod(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : IntArithExpression {
    override fun toViper() = Exp.Mod(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}


sealed interface IntComparisonExpression : ExpEmbedding {
    val left: ExpEmbedding
    val right: ExpEmbedding
    override val type
        get() = BooleanTypeEmbedding
}

data class LtCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : IntComparisonExpression {
    override fun toViper() = Exp.LtCmp(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}

data class LeCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : IntComparisonExpression {
    override fun toViper() = Exp.LeCmp(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}

data class GtCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : IntComparisonExpression {
    override fun toViper() = Exp.GtCmp(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}

data class GeCmp(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : IntComparisonExpression {
    override fun toViper() = Exp.GeCmp(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}


data class EqCmp(
    val left: ExpEmbedding,
    val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : ExpEmbedding {
    override val type = BooleanTypeEmbedding

    override fun toViper() = Exp.EqCmp(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}

data class NeCmp(
    val left: ExpEmbedding,
    val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : ExpEmbedding {
    override val type = BooleanTypeEmbedding

    override fun toViper() = Exp.NeCmp(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}


sealed interface BinaryBooleanExpression : ExpEmbedding {
    val left: ExpEmbedding
    val right: ExpEmbedding
    override val type: BooleanTypeEmbedding
        get() = BooleanTypeEmbedding
}

data class And(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : BinaryBooleanExpression {
    override fun toViper() = Exp.And(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}

data class Or(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : BinaryBooleanExpression {
    override fun toViper() = Exp.Or(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}

data class Implies(
    override val left: ExpEmbedding,
    override val right: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : BinaryBooleanExpression {
    override fun toViper() = Exp.Implies(left.toViper(), right.toViper(), Position.KtSourcePosition(pos))
}

data class Not(
    val exp: ExpEmbedding,
    override val pos: KtSourceElement? = null,
) : ExpEmbedding {
    override val type = BooleanTypeEmbedding

    override fun toViper() = Exp.Not(exp.toViper(), Position.KtSourcePosition(pos))
}


data object UnitLit : ExpEmbedding {
    override val type = UnitTypeEmbedding
    override val pos: KtSourceElement? = null

    override fun toViper() = UnitDomain.element
}

data class IntLit(val value: Int, override val pos: KtSourceElement? = null, ) : ExpEmbedding {
    override val type = IntTypeEmbedding

    override fun toViper() = Exp.IntLit(value)
}

data class BooleanLit(val value: Boolean, override val pos: KtSourceElement? = null, ) : ExpEmbedding {
    override val type = BooleanTypeEmbedding

    override fun toViper() = Exp.BoolLit(value)
}

data class NullLit(val elemType: TypeEmbedding, override val pos: KtSourceElement? = null) : ExpEmbedding {
    override val type = NullableTypeEmbedding(elemType)
    override fun toViper() = NullableDomain.nullVal(elemType.viperType)
}

data class Is(val exp: ExpEmbedding, val comparisonType: TypeEmbedding, override val pos: KtSourceElement? = null) : ExpEmbedding {
    override val type = BooleanTypeEmbedding

    override fun toViper() =
        TypeDomain.isSubtype(TypeOfDomain.typeOf(exp.toViper()), comparisonType.runtimeType)
}

data class Cast(val exp: ExpEmbedding, override val type: TypeEmbedding, override val pos: KtSourceElement? = null) : ExpEmbedding {
    override fun toViper() = CastingDomain.cast(exp.toViper(), type)
    override fun ignoringCasts(): ExpEmbedding = exp
}

data class FieldAccess(val receiver: ExpEmbedding, val field: FieldEmbedding, override val pos: KtSourceElement? = null) : ExpEmbedding {
    override val type: TypeEmbedding = field.type
    override fun toViper() = Exp.FieldAccess(receiver.toViper(), field.toViper(), Position.KtSourcePosition(pos))
    fun getAccessPredicate(perm: PermExp = PermExp.FullPerm()) = AccessPredicate.FieldAccessPredicate(toViper(), perm)
}