/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.callables

import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.formver.conversion.*
import org.jetbrains.kotlin.formver.embeddings.ExpEmbedding
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.Stmt

class InlineNamedFunction(
    val signature: FullNamedFunctionSignature,
    val symbol: FirFunctionSymbol<*>,
) : CallableEmbedding, FullNamedFunctionSignature by signature {
    @OptIn(SymbolInternals::class)
    override fun insertCallImpl(args: List<ExpEmbedding>, ctx: StmtConversionContext<ResultTrackingContext>): ExpEmbedding =
        ctx.withResult(returnType) {
            val inlineBody = symbol.fir.body ?: throw Exception("Function symbol $symbol has a null body")
            val paramNames = symbol.valueParameterSymbols.map { it.name }
            val callArgs = getInlineFunctionCallArgs(args)
            val subs = paramNames.zip(callArgs).toMap()
            val returnLabelName = ReturnLabelName(ctx.newWhileIndex())
            val newCtx = MethodConverter(
                this,
                this@InlineNamedFunction.signature,
                InlineParameterResolver(resultCtx.resultVar.name, returnLabelName, subs)
            )
            val inlineCtx = newBlock().withMethodContext(newCtx)
            inlineCtx.convert(inlineBody)
            // TODO: add these labels automatically.
            inlineCtx.addDeclaration(inlineCtx.returnLabel.toDecl())
            inlineCtx.addStatement(inlineCtx.returnLabel.toStmt())
            // Note: Putting the block inside the then branch of an if-true statement is a little hack to make Viper respect the scoping
            addStatement(Stmt.If(Exp.BoolLit(true), inlineCtx.block, Stmt.Seqn(listOf(), listOf())))
        }
}