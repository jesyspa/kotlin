/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.purity

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.formver.ErrorCollector
import org.jetbrains.kotlin.formver.embeddings.expression.Assert
import org.jetbrains.kotlin.formver.embeddings.expression.ExpEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.WithPosition

class PositionedExpEmbedding(val embedding: ExpEmbedding, val source: KtSourceElement?)

// Positioning information about a node is stored in the closest parent WithPosition node
fun ExpEmbedding.preorder(currentSource: KtSourceElement? = null): Sequence<PositionedExpEmbedding> = sequence {
    val nextSource: KtSourceElement? = when (this@preorder) {
        is WithPosition -> this@preorder.source
        else -> currentSource
    }
    yield(PositionedExpEmbedding(embedding = this@preorder, source = nextSource))

    yieldAll(this@preorder.children().flatMap { it.preorder(currentSource = nextSource) })
}

fun ExpEmbedding.checkValidity(declaration: FirSimpleFunction, errorCollector: ErrorCollector): Boolean =
    preorder(declaration.source)
        .all {
            it.embedding.isValid(DefaultPurityContext(it.source, errorCollector))
        }