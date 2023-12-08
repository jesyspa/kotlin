/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.viper.ast

import scala.collection.JavaConverters

/**
 * Interface used to wrap Viper nodes with an argument list (such as function applications, method calls, ...).
 */
interface IntoCallable {
    fun asCallable(): Callable
}

/**
 * Represents a Viper-callable object with arguments.
 */
interface Callable {
    val args: List<Node>

    fun arg(index: Int): Node {
        val arguments = args
        require(index in arguments.indices) {
            "The callable does not have a ${index + 1}-th argument."
        }
        return arguments[index]
    }

    /**
     * Extract Info metadata from the i-th argument list.
     */
    fun argInfo(index: Int): Info = arg(index).info
}

class CallableImpl(private val callableNode: viper.silver.ast.Node) : Callable {
    override val args: List<Node>
        get() {
            val arguments = when (callableNode) {
                is viper.silver.ast.FuncApp -> callableNode.args()
                is viper.silver.ast.MethodCall -> callableNode.args()
                else -> throw IllegalStateException("Unknown type for callable node.")
            }
            val wrappedArguments = JavaConverters.asJava(arguments).map { Node(it) }
            return wrappedArguments
        }
}

/**
 * Creates a new [IntoCallable] mixin to be used with the delegation pattern.
 */
fun delegateToCallable(callableNode: viper.silver.ast.Node): IntoCallable = object : IntoCallable {
    override fun asCallable(): Callable = CallableImpl(callableNode)
}