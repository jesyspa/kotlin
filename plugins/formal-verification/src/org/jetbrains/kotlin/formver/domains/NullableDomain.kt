/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.domains

import org.jetbrains.kotlin.formver.embeddings.NullableTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.TypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.TypeVarEmbedding
import org.jetbrains.kotlin.formver.scala.silicon.ast.*
import org.jetbrains.kotlin.formver.scala.silicon.ast.Exp.*
import org.jetbrains.kotlin.formver.scala.silicon.ast.Exp.Companion.Forall1
import org.jetbrains.kotlin.formver.scala.silicon.ast.Exp.Companion.Trigger1

/**
 * The domain in Viper code is as follows:
 *
 * ```
 * domain Nullable[T]  {
 *
 *   function null(): Nullable[T]
 *
 *   axiom some_not_null {
 *     (forall x: T ::
 *       { (cast(x): Nullable[T]) }
 *       (cast(x): Nullable[T]) !=
 *       (null(): Nullable[T]))
 *   }
 *
 *   axiom val_of_nullable_of_val {
 *     (forall x: T ::
 *       { (cast((cast(x): Nullable[T])): T) }
 *       (cast((cast(x): Nullable[T])): T) == x)
 *   }
 *
 *   axiom nullable_of_val_of_nullable {
 *     (forall nx: Nullable[T] ::
 *       { (cast((cast(nx): T)): Nullable[T]) }
 *       nx != (null(): Nullable[T]) ==>
 *       (cast((cast(nx): T)): Nullable[T]) == nx)
 *   }
 * }
 * ```
 */
object NullableDomain : BuiltinDomain("Nullable") {
    val T = TypeVarEmbedding("T")
    override val typeVars: List<Type.TypeVar> = listOf(T.type)

    private val xVar = Var("x", T)
    private val nxVar = Var("nx", NullableTypeEmbedding(T))

    private val nullFunc = createDomainFunc("null", emptyList(), NullableTypeEmbedding(T))
    override val functions: List<DomainFunc> = listOf(nullFunc)

    // You need to specify the type if the expression expects a certain nullable type,
    // e.g. in the expression x == null_val(), if x is of type type Nullable[Int], then
    // null_val() also needs to of type Nullable[Int] and can't be of type Nullable[T].
    fun nullVal(elemType: TypeEmbedding): DomainFuncApp =
        funcApp(nullFunc, emptyList(), NullableTypeEmbedding(elemType), mapOf(T.type to elemType.type))

    val someNotNull =
        createNamedDomainAxiom(
            "some_not_null",
            Forall1(
                xVar.decl(),
                Trigger1(CastingDomain.cast(xVar.use(), NullableTypeEmbedding(T))),
                NeCmp(CastingDomain.cast(xVar.use(), NullableTypeEmbedding(T)), nullVal(T))
            )
        )
    val valOfNullableOfVal =
        createNamedDomainAxiom(
            "val_of_nullable_of_val",
            Forall1(
                xVar.decl(),
                Trigger1(CastingDomain.cast(CastingDomain.cast(xVar.use(), NullableTypeEmbedding(T)), T)),
                EqCmp(CastingDomain.cast(CastingDomain.cast(xVar.use(), NullableTypeEmbedding(T)), T), xVar.use())
            )
        )
    val nullableOfValOfNullable =
        createNamedDomainAxiom(
            "nullable_of_val_of_nullable",
            Forall1(
                nxVar.decl(),
                Trigger1(CastingDomain.cast(CastingDomain.cast(nxVar.use(), T), NullableTypeEmbedding(T))),
                Implies(
                    NeCmp(nxVar.use(), nullVal(T)),
                    EqCmp(
                        CastingDomain.cast(CastingDomain.cast(nxVar.use(), T), NullableTypeEmbedding(T)),
                        nxVar.use()
                    )
                )
            )
        )
    override val axioms: List<DomainAxiom> = listOf(someNotNull, valOfNullableOfVal, nullableOfValOfNullable)
}