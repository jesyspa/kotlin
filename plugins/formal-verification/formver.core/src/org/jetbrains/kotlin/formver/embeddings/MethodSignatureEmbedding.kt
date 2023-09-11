/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.formver.conversion.ResultTrackingContext
import org.jetbrains.kotlin.formver.conversion.ReturnVariableName
import org.jetbrains.kotlin.formver.conversion.StmtConversionContext
import org.jetbrains.kotlin.formver.domains.convertType
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.*

/**
 * This embedding represents a method signature.
 * In case the method has a receiver it becomes the first argument of the function.
 * Example: Foo.bar(x: Int) --> Foo$bar(this: Foo, x: Int)
 */
interface MethodSignatureEmbedding {
    val name: MangledName
    val receiver: VariableEmbedding?
    val params: List<VariableEmbedding>
    val returnType: TypeEmbedding

    val returnVar
        get() = VariableEmbedding(ReturnVariableName, returnType)
    val formalArgs: List<VariableEmbedding>
        get() = listOfNotNull(receiver) + params

    fun toMethodCall(
        parameters: List<Exp>,
        targetVar: VariableEmbedding,
        pos: Position = Position.NoPosition,
        info: Info = Info.NoInfo,
        trafos: Trafos = Trafos.NoTrafos,
    ) = Stmt.MethodCall(name, parameters, listOf(targetVar.toLocalVar()), pos, info, trafos)
}

// This is pretty much a mess, but will get better when we have typed expression embeddings.
fun MethodSignatureEmbedding.callWithConversions(
    args: List<FirExpression>,
    ctx: StmtConversionContext<ResultTrackingContext>,
    call: (List<Exp>) -> Pair<Exp, TypeEmbedding>,
): Exp =
    args.zip(formalArgs)
        .map { (arg, param) -> ctx.convert(arg).convertType(ctx.embedType(arg), param.type) }
        .let(call)
        .let { (resultExp, resultType) -> resultExp.convertType(resultType, returnType) }
