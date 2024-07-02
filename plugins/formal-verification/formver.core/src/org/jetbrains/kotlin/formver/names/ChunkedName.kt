/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

data class ChunkedName(val chunks: List<NameChunk>) {
    fun assemble(): String = chunks.map { it.resolved }.filter { it != "" }.joinToString("$")

    constructor(vararg xs: NameChunk) : this(xs.toList())
    constructor(name: String) : this(listOf(StringNameChunk(name)))

    operator fun plus(other: ChunkedName): ChunkedName = ChunkedName(this.chunks + other.chunks)
}