/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.Purity

import org.jetbrains.kotlin.formver.embeddings.expression.Assert
import org.jetbrains.kotlin.formver.embeddings.expression.ExpEmbedding

fun ExpEmbedding.preorder(): Sequence<ExpEmbedding> = sequence {
    val stack = ArrayDeque<Iterator<ExpEmbedding>>()
    stack.addFirst(sequenceOf(this@preorder).iterator())

    while (stack.isNotEmpty()) {
        val it = stack.first()
        if (it.hasNext()) {
            val node = it.next()
            yield(node)

            val childIter = node.children().iterator()
            if (childIter.hasNext()) stack.addFirst(childIter)
        } else {
            stack.removeFirst()
        }
    }
}

fun ExpEmbedding.checkValidity(): Boolean =
    preorder()
        .filterIsInstance<Assert>()
        .all { it.checkOwnValidity() }