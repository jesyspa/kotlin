package org.jetbrains.kotlin.formver.purity

import org.jetbrains.kotlin.formver.embeddings.expression.*

class PurityChecker {
    // Restricted to single expressions only
    fun isPure(root: ExpEmbedding): Boolean = when (root) {
        is NullaryDirectResultExpEmbedding -> true
        is UnaryDirectResultExpEmbedding ->
            isPure(root.inner)
        is BinaryDirectResultExpEmbedding ->
            isPure(root.left) && isPure(root.right)
        is DirectResultExpEmbedding ->
            root.subexpressions.all { isPure(it) }
        is PassthroughExpEmbedding ->
            isPure(root.inner)
        else ->
            false
    }
}