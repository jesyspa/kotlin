/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

data class NamespacedName(val namespace: ResolvableNamespace, val name: Resolvable) : ResolvableImplMixin() {
    override val primary: ChunkedName
        get() = namespace.resolved + name.resolved

    override fun registerDependencies(resolver: NameResolver, phase: ResolutionPhase) {
        resolver.registerInPhase(namespace, namespace.phase)
        resolver.registerInPhase(name, InNamespacePhase(namespace))
    }
}

fun FqName.asChunkedName(): ChunkedName =
    ChunkedName(this.pathSegments().map { StringNameChunk(it.asStringStripSpecialMarkers()) })

fun Name.asChunk(): NameChunk = StringNameChunk(this.asStringStripSpecialMarkers())
fun Name.asChunkedName(): ChunkedName = ChunkedName(asChunk())
