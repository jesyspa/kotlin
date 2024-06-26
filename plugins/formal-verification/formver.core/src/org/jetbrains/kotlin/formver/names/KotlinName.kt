/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

import org.jetbrains.kotlin.formver.embeddings.TypeEmbedding
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Name corresponding to an entity in the original Kotlin code.
 *
 * This is a little more general than the `Name` type in Kotlin: we also use this
 * to represent getters and setters, for example.
 */
sealed interface KotlinName : MangledName

data class SimpleKotlinName(val name: Name) : KotlinName {
    override val mangled: String = name.asStringStripSpecialMarkers()
}

abstract class PrefixedKotlinName(prefix: String, name: Name) : KotlinName {
    override val mangled: String = "${prefix}_${name.asStringStripSpecialMarkers()}"
}

abstract class PrefixedKotlinNameWithType(prefix: String, name: Name, type: TypeEmbedding) : KotlinName {
    override val mangled: String = "${prefix}_${name.asStringStripSpecialMarkers()}\$${type.name.mangled}"
}

data class FunctionKotlinName(val name: Name, val type: TypeEmbedding) : PrefixedKotlinNameWithType("fun", name, type)

/**
 * This name will never occur in the viper output, but rather is used to lookup properties.
 */
data class PropertyKotlinName(val name: Name) : PrefixedKotlinName("property_property", name)
data class BackingFieldKotlinName(val name: Name) : PrefixedKotlinName("backing_field", name)
data class GetterKotlinName(val name: Name) : PrefixedKotlinName("property_getter", name)
data class SetterKotlinName(val name: Name) : PrefixedKotlinName("property_setter", name)
data class ExtensionSetterKotlinName(val name: Name, val type: TypeEmbedding) : PrefixedKotlinNameWithType(
    "ext_setter",
    name, type
)

data class ExtensionGetterKotlinName(val name: Name, val type: TypeEmbedding) : PrefixedKotlinNameWithType
    (
    "ext_getter",
    name, type
)

data class ClassKotlinName(val name: FqName) : KotlinName {
    override val mangled: String = "class_${name.asViperString()}"

    constructor(classSegments: List<String>) : this(FqName.fromSegments(classSegments))
}

data class ConstructorKotlinName(val type: TypeEmbedding) : KotlinName {
    override val mangled: String = "constructor\$${type.name.mangled}"
}

