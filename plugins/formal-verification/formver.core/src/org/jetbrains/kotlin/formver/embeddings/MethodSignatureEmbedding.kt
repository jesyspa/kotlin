/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.formver.conversion.ResultTrackingContext
import org.jetbrains.kotlin.formver.conversion.StmtConversionContext
import org.jetbrains.kotlin.formver.domains.convertType
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.Exp

/**
 * This embedding represents a method signature without parameter names.
 * In case the method has a receiver it becomes the first argument of the function.
 * Example: Foo.bar(x: Int) --> Foo$bar(this: Foo, x: Int)
 */
interface MethodSignatureEmbedding {
    val name: MangledName
    val receiverType: TypeEmbedding?
    val paramTypes: List<TypeEmbedding>
    val returnType: TypeEmbedding

    val formalArgTypes: List<TypeEmbedding>
        get() = listOfNotNull(receiverType) + paramTypes
}

// This is pretty much a mess, but will get better when we have typed expression embeddings.
fun MethodSignatureEmbedding.callWithConversions(
    args: List<FirExpression>,
    ctx: StmtConversionContext<ResultTrackingContext>,
    call: (List<Exp>) -> Pair<Exp, TypeEmbedding>,
): Exp =
    args.zip(formalArgTypes)
        .map { (arg, paramType) -> ctx.convert(arg).convertType(ctx.embedType(arg), paramType) }
        .let(call)
        .let { (resultExp, resultType) -> resultExp.convertType(resultType, returnType) }
