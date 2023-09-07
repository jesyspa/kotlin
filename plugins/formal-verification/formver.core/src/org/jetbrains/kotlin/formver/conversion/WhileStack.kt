/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

interface WhileStackContext {
    fun inNewWhileBlock(action: (Int) -> Unit)

    fun getWhile(): Int
}

class WhileStack(
    private val labelsIndexStack: MutableList<Int> = mutableListOf(),
    private var nextIndex: Int = 0
) : WhileStackContext {
    override fun inNewWhileBlock(action: (Int) -> Unit) {
        val index = nextIndex
        labelsIndexStack.add(nextIndex++)
        action(index)
        labelsIndexStack.removeLast()
    }

    override fun getWhile(): Int = labelsIndexStack.last()
}