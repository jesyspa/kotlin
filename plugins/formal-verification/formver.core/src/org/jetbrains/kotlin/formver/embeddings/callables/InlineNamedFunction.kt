/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.callables

import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.formver.conversion.StmtConversionContext
import org.jetbrains.kotlin.formver.conversion.insertInlineFunctionCall
import org.jetbrains.kotlin.formver.embeddings.expression.ExpEmbedding
import org.jetbrains.kotlin.formver.names.ExtraSpecialNames

class InlineNamedFunction(
    val signature: FullNamedFunctionSignature,
    val firBody: FirBlock,
) : RichCallableEmbedding, FullNamedFunctionSignature by signature {
    override fun insertCall(
        args: List<ExpEmbedding>,
        ctx: StmtConversionContext,
    ): ExpEmbedding {
        val paramNames = buildList {
            if (callableType.dispatchReceiverType != null)
                add(ExtraSpecialNames.DISPATCH_THIS)
            if (callableType.extensionReceiverType != null)
                add(ExtraSpecialNames.EXTENSION_THIS)
            addAll(symbol.valueParameterSymbols.map { it.name })
        }
        return ctx.insertInlineFunctionCall(signature, paramNames, args, firBody, signature.labelName)
    }

    override fun toViperMethodHeader(): Nothing? = null
}