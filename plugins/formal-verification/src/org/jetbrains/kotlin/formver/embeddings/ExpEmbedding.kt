/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.formver.domains.NullableDomain
import org.jetbrains.kotlin.formver.domains.UnitDomain
import org.jetbrains.kotlin.formver.scala.silicon.ast.*
import org.jetbrains.kotlin.formver.scala.toScalaBigInt
import viper.silver.ast.DomainType
import javax.sound.sampled.LineEvent.Type

sealed interface ExpEmbedding {
    val type: TypeEmbedding
    val viperExp: Exp

    fun withType(newType: TypeEmbedding): ExpEmbedding =
        when {
            type == newType -> this
            type !is NullableTypeEmbedding && newType is NullableTypeEmbedding -> NullableOf(this.withType(newType.elementType))
            // This NullLit conversion is just hard-coded here to not break the tests,
            // it can be deleted once the to-do below has been solved.
            this is NullLit && newType is NullableTypeEmbedding -> NullLit(newType)
            type is NullableTypeEmbedding && (type as NullableTypeEmbedding).elementType.isSubTypeOf(newType) ->
                ValOfNullable(this).withType(newType)
            type is NullableTypeEmbedding && newType is NullableTypeEmbedding -> TODO("Add domain function in the Nullable domain to convert from one type of nullable to another if their element types are subtypes.")
            type.isSubTypeOf(newType) -> throw NotImplementedError("Type $type is a subtype of $newType but no conversion function was specified.")
            else -> throw IllegalArgumentException("Expression $this of type $type is not a subtype of $newType and can thus not be converted to it.")
        }
}

fun <E : ExpEmbedding> List<E>.viperExps(): List<Exp> = map { it.viperExp }

data class Add(val left: ExpEmbedding, val right: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = commonSuperType(left.type, right.type)
    override val viperExp: Exp.Add =
        Exp.Add(left.withType(type).viperExp, right.withType(type).viperExp)
}

data class Sub(val left: ExpEmbedding, val right: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = commonSuperType(left.type, right.type)
    override val viperExp: Exp.Sub =
        Exp.Sub(left.withType(type).viperExp, right.withType(type).viperExp)
}

data class Mul(val left: ExpEmbedding, val right: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = commonSuperType(left.type, right.type)
    override val viperExp: Exp.Mul =
        Exp.Mul(left.withType(type).viperExp, right.withType(type).viperExp)
}

data class Div(val left: ExpEmbedding, val right: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = commonSuperType(left.type, right.type)
    override val viperExp: Exp.Div =
        Exp.Div(left.withType(type).viperExp, right.withType(type).viperExp)
}

data class Mod(val left: ExpEmbedding, val right: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = commonSuperType(left.type, right.type)
    override val viperExp: Exp.Mod =
        Exp.Mod(left.withType(type).viperExp, right.withType(type).viperExp)
}

data class LtCmp(val left: ExpEmbedding, val right: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = BooleanTypeEmbedding
    val cmpArgType = commonSuperType(left.type, right.type)
    override val viperExp: Exp.LtCmp =
        Exp.LtCmp(left.withType(cmpArgType).viperExp, right.withType(cmpArgType).viperExp)
}

data class LeCmp(val left: ExpEmbedding, val right: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = BooleanTypeEmbedding
    val cmpArgType = commonSuperType(left.type, right.type)
    override val viperExp: Exp.LeCmp =
        Exp.LeCmp(left.withType(cmpArgType).viperExp, right.withType(cmpArgType).viperExp)
}

data class GtCmp(val left: ExpEmbedding, val right: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = BooleanTypeEmbedding
    val cmpArgType = commonSuperType(left.type, right.type)
    override val viperExp: Exp.GtCmp =
        Exp.GtCmp(left.withType(cmpArgType).viperExp, right.withType(cmpArgType).viperExp)
}

data class GeCmp(val left: ExpEmbedding, val right: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = BooleanTypeEmbedding
    val cmpArgType = commonSuperType(left.type, right.type)
    override val viperExp: Exp.GeCmp =
        Exp.GeCmp(left.withType(cmpArgType).viperExp, right.withType(cmpArgType).viperExp)
}

data class EqCmp(val left: ExpEmbedding, val right: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = BooleanTypeEmbedding
    val cmpArgType = commonSuperType(left.type, right.type)
    override val viperExp: Exp.EqCmp =
        Exp.EqCmp(left.withType(cmpArgType).viperExp, right.withType(cmpArgType).viperExp)
}

data class NeCmp(val left: ExpEmbedding, val right: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = BooleanTypeEmbedding
    val cmpArgType = commonSuperType(left.type, right.type)
    override val viperExp: Exp.NeCmp =
        Exp.NeCmp(left.withType(cmpArgType).viperExp, right.withType(cmpArgType).viperExp)
}

data class And(val left: ExpEmbedding, val right: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = BooleanTypeEmbedding
    override val viperExp: Exp.And =
        Exp.And(left.viperExp, right.viperExp)
}

data class Implies(val left: ExpEmbedding, val right: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = BooleanTypeEmbedding
    override val viperExp: Exp.Implies =
        Exp.Implies(left.viperExp, right.viperExp)
}

data class Not(val arg: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = BooleanTypeEmbedding
    override val viperExp: Exp.Not =
        Exp.Not(arg.viperExp)
}

data class Trigger(val exps: List<ExpEmbedding>) {
    val viperExp: Exp.Trigger =
        Exp.Trigger(exps.viperExps())
}

data class Forall(val boundVariable: List<VariableEmbedding>, val triggers: List<Trigger>, val exp: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = BooleanTypeEmbedding
    override val viperExp: Exp.Forall =
        Exp.Forall(
            boundVariable.map { it.toLocalVarDecl() },
            triggers.map { it.viperExp },
            exp.viperExp
        )
}

data class Exists(val boundVariable: List<VariableEmbedding>, val triggers: List<Trigger>, val exp: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = BooleanTypeEmbedding
    override val viperExp: Exp.Exists =
        Exp.Exists(
            boundVariable.map { it.toLocalVarDecl() },
            triggers.map { it.viperExp },
            exp.viperExp
        )
}

data class IntLit(val value: Int) : ExpEmbedding {
    override val type: TypeEmbedding = IntTypeEmbedding
    override val viperExp: Exp.IntLit = Exp.IntLit(value.toScalaBigInt())
}

data class BoolLit(val value: Boolean) : ExpEmbedding {
    override val type: TypeEmbedding = BooleanTypeEmbedding
    override val viperExp: Exp.BoolLit = Exp.BoolLit(value)
}

data object UnitLit : ExpEmbedding {
    override val type: TypeEmbedding = UnitTypeEmbedding
    override val viperExp: Exp = UnitDomain.element
}

data class NullableOf(val nonNullExp: ExpEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = NullableTypeEmbedding(nonNullExp.type)
    override val viperExp = NullableDomain.nullableOfApp(nonNullExp.viperExp, nonNullExp.type.type)
}

data class ValOfNullable(val exp: ExpEmbedding) : ExpEmbedding {
    val elemType = (exp.type as NullableTypeEmbedding).elementType
    override val type: TypeEmbedding = elemType
    override val viperExp = NullableDomain.valOfApp(exp.viperExp, elemType.type)
}

// This class represents the Kotlin null literal and not the Viper null literal
// and is in fact not even mapped to in but instead to a custom Nullable domain value.
data class NullLit(override val type: NullableTypeEmbedding) : ExpEmbedding {
    override val viperExp: Exp = NullableDomain.nullVal(type.elementType.type)
}

data class FieldAccess(val rcv: ExpEmbedding, val field: FieldEmbedding) : ExpEmbedding {
    override val type: TypeEmbedding = field.type
    override val viperExp: Exp.FieldAccess =
        Exp.FieldAccess(rcv.viperExp, field.field)
}