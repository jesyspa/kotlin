/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.viper.ast

import scala.collection.Seq
import viper.silver.ast.Exp

/**
 * Interface used to wrap Viper nodes with an argument list (such as function applications, method calls, ...).
 */
interface IntoSilverCallable {
    fun asCallable(): SilverCallable
}

/**
 * Represents a Viper-callable object with arguments.
 */
interface SilverCallable {
    val args: Seq<Exp>

    fun arg(index: Int): Exp {
        val arguments = args
        require(index in 0..<arguments.size()) {
            "The callable does not have a ${index + 1}-th argument."
        }
        return arguments.apply(index)
    }

    /**
     * Extract Info metadata from the i-th argument list.
     */
    fun argInfo(index: Int): Info = Info.fromSilver(arg(index).info)
}

class SilverCallableImpl(private val callableNode: viper.silver.ast.Node) : SilverCallable {
    override val args: Seq<Exp>
        get() = when (callableNode) {
            is viper.silver.ast.FuncApp -> callableNode.args()
            is viper.silver.ast.MethodCall -> callableNode.args()
            else -> throw IllegalStateException("Unknown type for callable node.")
        }
}

/**
 * Creates a new [IntoSilverCallable] mixin to be used with the delegation pattern.
 */
fun delegateToSilverCallable(callableNode: viper.silver.ast.Node): IntoSilverCallable = object : IntoSilverCallable {
    override fun asCallable(): SilverCallable = SilverCallableImpl(callableNode)
}