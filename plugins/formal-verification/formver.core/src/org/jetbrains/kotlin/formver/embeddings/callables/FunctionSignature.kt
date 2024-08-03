/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.callables

import org.jetbrains.kotlin.formver.embeddings.TypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.FirVariableEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.VariableEmbedding

interface FunctionSignature : CallableSignature {
    val dispatchReceiver: VariableEmbedding?
    val extensionReceiver: VariableEmbedding?

    val params: List<FirVariableEmbedding>

    val sourceName: String?
        get() = null

    val formalArgs: List<VariableEmbedding>
        get() = listOfNotNull(dispatchReceiver, extensionReceiver) + params

    override val dispatchReceiverType: TypeEmbedding?
        get() = dispatchReceiver?.type
    override val extensionReceiverType: TypeEmbedding?
        get() = extensionReceiver?.type
    override val paramTypes: List<TypeEmbedding>
        get() = params.map { it.type }
}
