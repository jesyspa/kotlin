/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.types

import org.jetbrains.kotlin.formver.embeddings.expression.OperatorExpEmbeddings.StringLength

/**
 * Sometimes we want to enhance a predicate with additional assertions.
 * Hence, we introduce `ClassPredicateEnhancer`.
 */
sealed class ClassPredicateEnhancer {
    internal abstract fun applyAdditionalAssertions(builder: ClassPredicateBuilder)
}

data object StringSharedPredicateEnhancer : ClassPredicateEnhancer() {
    override fun applyAdditionalAssertions(builder: ClassPredicateBuilder) {
        builder.apply {
            forUserFieldNamed("length") {
                addEqualsGuarantee {
                    StringLength(this)
                }
            }
        }
    }
}
