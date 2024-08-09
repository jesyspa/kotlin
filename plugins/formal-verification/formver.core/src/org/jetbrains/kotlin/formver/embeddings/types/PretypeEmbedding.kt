/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.types

import org.jetbrains.kotlin.formver.embeddings.SubTypeInvariantEmbedding
import org.jetbrains.kotlin.formver.embeddings.TypeInvariantEmbedding
import org.jetbrains.kotlin.formver.embeddings.TypeInvariantHolder
import org.jetbrains.kotlin.formver.embeddings.expression.debug.PlaintextLeaf
import org.jetbrains.kotlin.formver.embeddings.expression.debug.TreeView
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.mangled

/**
 * A representation of a Kotlin type without nullability and uniqueness information.
 *
 * We explicitly choose not to make this a subtype of `TypeEmbedding`, even though there is a simple way of treating
 * every `PretypeEmbedding` as a `TypeEmbedding`: the goal of the separation into types and pretypes is to avoid
 * one showing up where the other is expected.  For example, the naming systems are different, and the equality
 * comparisons would not work.
 */
interface PretypeEmbedding : RuntimeTypeHolder, TypeInvariantHolder {
    val name: MangledName

    override val debugTreeView: TreeView
        get() = PlaintextLeaf(name.mangled)

    override fun subTypeInvariant(): TypeInvariantEmbedding = SubTypeInvariantEmbedding(this)
}

