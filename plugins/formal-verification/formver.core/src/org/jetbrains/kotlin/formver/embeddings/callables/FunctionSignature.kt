/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.callables

import org.jetbrains.kotlin.formver.embeddings.FunctionTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.PlaceholderVariableEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.VariableEmbedding
import org.jetbrains.kotlin.formver.names.AnonymousName
import org.jetbrains.kotlin.formver.names.ThisReceiverName

interface FunctionSignature {
    val type: FunctionTypeEmbedding
    val receiver: VariableEmbedding?
    val params: List<VariableEmbedding>

    val sourceName: String?
        get() = null

    val formalArgs: List<VariableEmbedding>
        get() = listOfNotNull(receiver) + params
}

abstract class GenericFunctionSignatureMixin : FunctionSignature {
    override val receiver: VariableEmbedding?
        get() = type.receiverType?.let { PlaceholderVariableEmbedding(ThisReceiverName, it) }

    override val params: List<VariableEmbedding>
        get() = type.paramTypes.mapIndexed { ix, type -> PlaceholderVariableEmbedding(AnonymousName(ix), type) }
}