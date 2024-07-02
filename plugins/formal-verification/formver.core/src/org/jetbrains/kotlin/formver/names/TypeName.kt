/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

data class TypeName(val details: DetailedTypeName, val nullable: Boolean = false) : ResolvableImplMixin() {
    override val primary = ChunkedName(nullabilityChunk) + details.name

    private val nullabilityChunk
        get() = StringNameChunk(if (nullable) "NT" else "T")
}

interface DetailedTypeName {
    val name: ChunkedName
}

data class SimpleTypeName(val baseName: String) : DetailedTypeName {
    override val name = ChunkedName(baseName)
}

data class FunctionTypeName(val typeParameters: List<TypeName>) : DetailedTypeName {
    override val name = ChunkedName( buildList {
        add(StringNameChunk("fun_type"))
        for (type in typeParameters) {
            addAll(type.primary.chunks)
        }
    })
}

data class ClassTypeName(val className: )