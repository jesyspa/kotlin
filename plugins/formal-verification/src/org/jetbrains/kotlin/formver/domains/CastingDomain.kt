/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.domains

import org.jetbrains.kotlin.formver.embeddings.NullableTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.TypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.TypeVarEmbedding
import org.jetbrains.kotlin.formver.scala.silicon.ast.*

object CastingDomain : BuiltinDomain("Casting") {
    private val A = TypeVarEmbedding("A")
    private val B = TypeVarEmbedding("B")
    override val typeVars: List<Type.TypeVar> = listOf(A.type, B.type)

    private val castFunc = createDomainFunc("cast", listOf(Var("a", A).decl()), B)

    fun cast(exp: Exp, newType: TypeEmbedding) =
        funcApp(castFunc, listOf(exp), newType, mapOf(A.type to exp.type.type, B.type to newType.type))

    override val functions: List<DomainFunc> = listOf(castFunc)

    /**
     * Casting a null value to a different nullable type results in the null value again.
     *
     * axiom null_cast {
     *   forall a: A ::
     *       (cast((cast(a) : B)) : Nullable[B]) == (cast((cast(a) : Nullable[A])) : Nullable[B])
     * }
     */
    private val nullCast =
        createNamedDomainAxiom(
            "null_cast",
            Exp.EqCmp(cast(NullableDomain.nullVal(A), NullableTypeEmbedding(B)), NullableDomain.nullVal(B))
        )

    override val axioms: List<DomainAxiom> = listOf(nullCast)
}