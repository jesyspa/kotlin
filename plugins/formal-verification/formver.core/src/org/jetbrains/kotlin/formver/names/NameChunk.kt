/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

sealed interface NameChunk {
    val resolved: String
}

data class StringNameChunk(override val resolved: String) : NameChunk

data class ResolvableNameChunk(val resolvable: Resolvable) : NameChunk {
    override val resolved: String
        get() = resolvable.resolved.assemble()
}