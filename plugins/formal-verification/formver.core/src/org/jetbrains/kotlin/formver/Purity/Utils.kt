/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.purity

import org.jetbrains.kotlin.formver.embeddings.expression.Assert
import org.jetbrains.kotlin.formver.embeddings.expression.ExpEmbedding

fun ExpEmbedding.preorder(): Sequence<ExpEmbedding> = sequence {
    yield(this@preorder)
    for (c in children()) {
        yieldAll(c.preorder())
    }
}

fun ExpEmbedding.checkValidity(): Boolean =
    preorder()
        .filterIsInstance<Assert>()
        .all { it.isValid() }