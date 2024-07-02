/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

import org.jetbrains.kotlin.formver.viper.MangledName

interface Resolvable : MangledName {
    val primary: ChunkedName
    val secondary: List<ChunkedName>
        get() = listOf()

    val resolved: ChunkedName
    fun commitResolution(name: ChunkedName)

    fun registerDependencies(resolver: NameResolver, phase: ResolutionPhase) {}
}

interface ResolvableNamespace : Resolvable {
    val phase: ResolutionPhase
    val optional: Boolean
        get() = false
}

abstract class ResolvableImplMixin : Resolvable {
    private var _resolved: ChunkedName? = null
    final override val resolved: ChunkedName
        get() = _resolved ?: error("$this is not resolved yet")

    final override fun commitResolution(name: ChunkedName) {
        require(_resolved == null) { "$this is already resolved" }
        _resolved = name
    }

    final override val mangled: String by lazy { resolved.assemble() }
}