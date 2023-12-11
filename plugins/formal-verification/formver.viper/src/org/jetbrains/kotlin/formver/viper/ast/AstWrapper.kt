/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.viper.ast

/**
 * Interface acting as a wrapper between Kotlin and Silver's defined data-structure for the Abstract Syntax Tree.
 */
sealed interface AstWrapper {
    open class Node(val node: viper.silver.ast.Node) : AstWrapper, IntoCallable by delegateToCallable(node) {
        fun <I> getInfoOrNull(): I? = info.unwrapOr<I> { null }
    }

    data class Exp(val exp: viper.silver.ast.Exp) : Node(exp) {
        constructor(node: viper.silver.ast.Node) : this(node as viper.silver.ast.Exp)
    }
}