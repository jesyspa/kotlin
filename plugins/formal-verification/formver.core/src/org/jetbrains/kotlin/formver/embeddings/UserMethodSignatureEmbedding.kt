/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.formver.conversion.ReturnVariableName
import org.jetbrains.kotlin.formver.viper.ast.*

interface UserMethodSignatureEmbedding : MethodSignatureEmbedding {
    val receiver: VariableEmbedding?
    val params: List<VariableEmbedding>

    val returnVar
        get() = VariableEmbedding(ReturnVariableName, returnType)
    val formalArgs: List<VariableEmbedding>
        get() = listOfNotNull(receiver) + params

    override val receiverType: TypeEmbedding?
        get() = receiver?.type
    override val paramTypes: List<TypeEmbedding>
        get() = params.map { it.type }
}

fun UserMethodSignatureEmbedding.toMethodCall(
    parameters: List<Exp>,
    targetVar: VariableEmbedding,
    pos: Position = Position.NoPosition,
    info: Info = Info.NoInfo,
    trafos: Trafos = Trafos.NoTrafos,
) = Stmt.MethodCall(name, parameters, listOf(targetVar.toLocalVar()), pos, info, trafos)

