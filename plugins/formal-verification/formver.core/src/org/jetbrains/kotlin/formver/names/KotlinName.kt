/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

import org.jetbrains.kotlin.formver.embeddings.TypeEmbedding
import org.jetbrains.kotlin.name.Name

/**
 * Name corresponding to an entity in the original Kotlin code.
 *
 * This is a little more general than the `Name` type in Kotlin: we also use this
 * to represent getters and setters, for example.
 */

data class SimpleKotlinName(val name: Name) : ResolvableImplMixin() {
    override val primary = name.asChunkedName()
}

abstract class PrefixedKotlinName(prefix: String, name: Name) : ResolvableImplMixin() {
    override val primary = ChunkedName(StringNameChunk(prefix), name.asChunk())
    override val secondary = listOf(ChunkedName(name.asChunk()))
}

abstract class PrefixedKotlinNameWithType(prefix: String, name: Name, type: TypeEmbedding) : ResolvableImplMixin() {
    override val primary = ChunkedName(StringNameChunk(prefix), name.asChunk(), ResolvableNameChunk(type.name))
    override val secondary = listOf(name.asChunkedName(), ChunkedName(StringNameChunk(prefix), name.asChunk()))
}

data class FunctionKotlinName(val name: Name, val type: TypeEmbedding) : PrefixedKotlinNameWithType("fun", name, type)

/**
 * This name will never occur in the viper output, but rather is used to lookup properties.
 */
data class PropertyKotlinName(val name: Name) : PrefixedKotlinName("property_property", name)
data class BackingFieldKotlinName(val name: Name) : PrefixedKotlinName("backing_field", name)
data class GetterKotlinName(val name: Name) : PrefixedKotlinName("property_getter", name)
data class SetterKotlinName(val name: Name) :PrefixedKotlinName("property_setter", name)
data class ExtensionSetterKotlinName(val name: Name, val type: TypeEmbedding) : PrefixedKotlinNameWithType(
    "ext_setter",
    name, type
)

data class ExtensionGetterKotlinName(val name: Name, val type: TypeEmbedding) : PrefixedKotlinNameWithType
    (
    "ext_getter",
    name, type
)

data class ConstructorKotlinName(val type: TypeEmbedding) : ResolvableImplMixin() {
    override val primary = ChunkedName(StringNameChunk("constructor"), ResolvableNameChunk(type.name))
    override val secondary = listOf(ChunkedName("constructor"), ChunkedName(StringNameChunk("constructor"),
                                                                            ResolvableNameChunk(type.name)))
}
