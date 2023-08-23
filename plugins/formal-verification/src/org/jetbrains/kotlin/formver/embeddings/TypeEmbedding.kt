/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.formver.conversion.SpecialFields
import org.jetbrains.kotlin.formver.domains.NullableDomain
import org.jetbrains.kotlin.formver.domains.UnitDomain
import org.jetbrains.kotlin.formver.scala.silicon.ast.Exp
import org.jetbrains.kotlin.formver.scala.silicon.ast.PermExp
import org.jetbrains.kotlin.formver.scala.silicon.ast.Type

interface TypeEmbedding {
    val type: Type
    fun invariants(v: Exp): List<Exp> = emptyList()

    /**
     * Invariants that should correlate the old and new value of a value of this type
     * over a function call.  This is exclusively necessary for CallsInPlace.
     *
     * TODO: add support for these in loops, too.
     */
    fun dynamicInvariants(v: Exp): List<Exp> = emptyList()

    fun isSubTypeOf(otherType: TypeEmbedding): Boolean
}

fun commonSuperType(type1: TypeEmbedding, type2: TypeEmbedding): TypeEmbedding =
    when {
        // Info: There are more nuanced situations like finding the common super type of Nothing? and Int which is Int?.
        // These situations are not handled here
        type1.isSubTypeOf(type2) -> type2
        type2.isSubTypeOf(type1) -> type1
        else -> throw IllegalArgumentException("Cannot find common super type of $type1 and $type2.")
    }

data object UnitTypeEmbedding : TypeEmbedding {
    override val type: Type = UnitDomain.toType()

    override fun isSubTypeOf(otherType: TypeEmbedding): Boolean =
        otherType is UnitTypeEmbedding || otherType is NullableTypeEmbedding && this.isSubTypeOf(otherType.elementType)
}

data object NothingTypeEmbedding : TypeEmbedding {
    override val type: Type = UnitDomain.toType()

    override fun invariants(v: Exp): List<Exp> = listOf(Exp.BoolLit(false))

    override fun isSubTypeOf(otherType: TypeEmbedding): Boolean = true
}

data object IntTypeEmbedding : TypeEmbedding {
    override val type: Type = Type.Int

    override fun isSubTypeOf(otherType: TypeEmbedding): Boolean =
        otherType is IntTypeEmbedding || otherType is NullableTypeEmbedding && this.isSubTypeOf(otherType.elementType)
}

data object BooleanTypeEmbedding : TypeEmbedding {
    override val type: Type = Type.Bool

    override fun isSubTypeOf(otherType: TypeEmbedding): Boolean =
        otherType is BooleanTypeEmbedding || otherType is NullableTypeEmbedding && this.isSubTypeOf(otherType.elementType)
}

data class TypeVarEmbedding(val name: String) : TypeEmbedding {
    override val type: Type.TypeVar = Type.TypeVar(name)

    override fun isSubTypeOf(otherType: TypeEmbedding): Boolean =
        otherType == this || otherType is NullableTypeEmbedding && this.isSubTypeOf(otherType.elementType)
}

data class NullableTypeEmbedding(val elementType: TypeEmbedding) : TypeEmbedding {
    override val type: Type = NullableDomain.toType(mapOf(NullableDomain.T.type to elementType.type))

    override fun isSubTypeOf(otherType: TypeEmbedding): Boolean =
        otherType is NullableTypeEmbedding && this.elementType.isSubTypeOf(otherType.elementType)
}

data object FunctionTypeEmbedding : TypeEmbedding {
    override val type: Type = Type.Ref

    override fun invariants(v: Exp): List<Exp> =
        listOf(v.fieldAccessPredicate(SpecialFields.FunctionObjectCallCounterField, PermExp.FullPerm()))

    override fun dynamicInvariants(v: Exp): List<Exp> =
        listOf(
            Exp.LeCmp(
                Exp.Old(v.fieldAccess(SpecialFields.FunctionObjectCallCounterField)),
                v.fieldAccess(SpecialFields.FunctionObjectCallCounterField)
            )
        )

    override fun isSubTypeOf(otherType: TypeEmbedding): Boolean = false
}