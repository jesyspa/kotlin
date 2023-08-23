/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.scala.silicon.ast

import org.jetbrains.kotlin.formver.embeddings.BooleanTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.TypeEmbedding

sealed interface AccessPredicate : Exp {
    override fun toViper(): viper.silver.ast.AccessPredicate

    data class FieldAccessPredicate(
        val access: Exp.FieldAccess,
        val perm: PermExp,
        val pos: Position = Position.NoPosition,
        val info: Info = Info.NoInfo,
        val trafos: Trafos = Trafos.NoTrafos,
    ) : AccessPredicate {
        override val type: TypeEmbedding
            get() = throw IllegalAccessError("A field access predicate expression does not have a type.")

        override fun toViper(): viper.silver.ast.AccessPredicate =
            viper.silver.ast.FieldAccessPredicate(access.toViper(), perm.toViper(), pos.toViper(), info.toViper(), trafos.toViper())
    }
}