/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.formver.scala.MangledName
import org.jetbrains.kotlin.formver.scala.silicon.ast.*
import org.jetbrains.kotlin.formver.scala.silicon.ast.Exp

class VariableEmbedding(val name: MangledName, override val type: TypeEmbedding) : ExpEmbedding {
    fun toLocalVarDecl(
        pos: Position = Position.NoPosition,
        info: Info = Info.NoInfo,
        trafos: Trafos = Trafos.NoTrafos,
    ): Declaration.LocalVarDecl = Declaration.LocalVarDecl(name, type.type, pos, info, trafos)

    fun toLocalVar(
        pos: Position = Position.NoPosition,
        info: Info = Info.NoInfo,
        trafos: Trafos = Trafos.NoTrafos,
    ): Exp.LocalVar = Exp.LocalVar(name, type.type, pos, info, trafos)

    override val viperExp: Exp.LocalVar = toLocalVar()

    fun invariants(): List<Exp> = type.invariants(toLocalVar())
    fun dynamicInvariants(): List<Exp> = type.dynamicInvariants(toLocalVar())
}