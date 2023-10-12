/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.formver.conversion.SpecialFields
import org.jetbrains.kotlin.formver.isCollection
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.Field

// inhalePolicy is true when it is necessary to inhale permission before accessing the field
class FieldEmbedding(val name: MangledName, val type: TypeEmbedding, val inhalePolicy: Boolean = true) {
    fun toViper(): Field = Field(name, type.viperType)
}

fun FirPropertySymbol.specialEmbedding(): FieldEmbedding? {
    // in the future, new special properties can be added here (e.g. String.length)
    return when {
        callableId.isCollection -> when (callableId.callableName.asString()) {
            "size" -> FieldEmbedding(SpecialFields.ListSizeField.name, IntTypeEmbedding, false)
            else -> null
        }
        else -> null
    }
}