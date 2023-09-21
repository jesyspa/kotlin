/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.formver.conversion.*
import org.jetbrains.kotlin.formver.embeddings.callables.CallableEmbedding
import org.jetbrains.kotlin.formver.embeddings.callables.FunctionSignature
import org.jetbrains.kotlin.formver.embeddings.callables.asData
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.Stmt

class LambdaExp(
    val signature: FunctionSignature,
    val function: FirAnonymousFunction,
    private val parentCtx: MethodConversionContext,
) : CallableEmbedding, ExpEmbedding,
    FunctionSignature by signature {
    override val type: TypeEmbedding = FunctionTypeEmbedding(signature.asData)

    override fun toViper(): Exp = TODO("create new function object with counter, duplicable (requires toViper restructuring)")

    override fun insertCallImpl(args: List<ExpEmbedding>, ctx: StmtConversionContext<ResultTrackingContext>): ExpEmbedding =
        ctx.withResult(returnType) {
            val inlineBody = function.body ?: throw Exception("Lambda $function has a null body")
            val paramNames = function.valueParameters.map { it.name }
            val callArgs = ctx.getInlineFunctionCallArgs(args)
            val subs = paramNames.zip(callArgs).toMap()
            val returnLabelName = ReturnLabelName(newWhileIndex())
            val newCtx = MethodConverter(
                this, this@LambdaExp.signature,
                InlineParameterResolver(this.resultCtx.resultVar.name, returnLabelName, subs),
                parentCtx
            )
            val lambdaCtx = this.newBlock().withMethodContext(newCtx)
            lambdaCtx.convert(inlineBody)
            lambdaCtx.addDeclaration(lambdaCtx.returnLabel.toDecl())
            lambdaCtx.addStatement(lambdaCtx.returnLabel.toStmt())
            // NOTE: Putting the block inside the then branch of an if-true statement is a little hack to make Viper respect the scoping
            addStatement(Stmt.If(Exp.BoolLit(true), lambdaCtx.block, Stmt.Seqn(listOf(), listOf())))
        }
}