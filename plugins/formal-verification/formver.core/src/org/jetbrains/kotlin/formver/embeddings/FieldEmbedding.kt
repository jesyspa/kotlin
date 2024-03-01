/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.formver.conversion.AccessPolicy
import org.jetbrains.kotlin.formver.embeddings.expression.*
import org.jetbrains.kotlin.formver.names.*
import org.jetbrains.kotlin.formver.names.NameMatcher
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.Field
import org.jetbrains.kotlin.formver.viper.ast.PermExp

/**
 * Embedding of a backing field of a property.
 */
interface FieldEmbedding {
    val name: MangledName
    val type: TypeEmbedding
    val accessPolicy: AccessPolicy
    val includeInShortDump: Boolean
    val symbol: FirPropertySymbol?
        get() = null

    fun toViper(): Field = Field(name, type.viperType, includeInShortDump)

    fun extraAccessInvariantsForParameter(): List<TypeInvariantEmbedding> = listOf()

    fun accessInvariantsForParameter(): List<TypeInvariantEmbedding> =
        when (accessPolicy) {
            AccessPolicy.ALWAYS_INHALE_EXHALE -> listOf()
            AccessPolicy.ALWAYS_READABLE -> listOf(
                FieldAccessTypeInvariantEmbedding(this, PermExp.WildcardPerm())
            )
            AccessPolicy.ALWAYS_WRITEABLE -> listOf(FieldAccessTypeInvariantEmbedding(this, PermExp.FullPerm()))
        } + extraAccessInvariantsForParameter()

    fun accessInvariantForAccess(): TypeInvariantEmbedding? =
        when (accessPolicy) {
            AccessPolicy.ALWAYS_INHALE_EXHALE -> FieldAccessTypeInvariantEmbedding(this, PermExp.FullPerm())
            AccessPolicy.ALWAYS_READABLE, AccessPolicy.ALWAYS_WRITEABLE -> null
        }

    /**
     * Returns all types in which this field is an argument of the primary constructor.
     *
     * This is necessary to provide additional invariants for constructor methods.
     *
     * Note that field can be an argument of the primary constructor not only
     * in the class it was first declared but also in some of its descendants
     * (via `override`). Thus, we store a map and not a single value.
     */
    fun getTypesContainingAsPrimaryConstructorArg(): Map<TypeEmbedding, FirPropertySymbol> = emptyMap()
}

class UserFieldEmbedding(
    override val name: ScopedKotlinName,
    override val type: TypeEmbedding,
    override val symbol: FirPropertySymbol
) : FieldEmbedding {
    override val accessPolicy: AccessPolicy = if (symbol.isVal) AccessPolicy.ALWAYS_READABLE else AccessPolicy.ALWAYS_INHALE_EXHALE
    override val includeInShortDump: Boolean = true

    private val _typesContainingAsPrimaryConstructorArg = mutableMapOf<TypeEmbedding, FirPropertySymbol>()

    override fun getTypesContainingAsPrimaryConstructorArg(): Map<TypeEmbedding, FirPropertySymbol> =
        _typesContainingAsPrimaryConstructorArg

    fun registerTypeContainingAsPrimaryConstructorArg(type: TypeEmbedding, symbol: FirPropertySymbol) {
        _typesContainingAsPrimaryConstructorArg[type] = symbol
    }
}


object ListSizeFieldEmbedding : FieldEmbedding {
    override val name = SpecialName("size")
    override val type = IntTypeEmbedding
    override val accessPolicy = AccessPolicy.ALWAYS_WRITEABLE
    override val includeInShortDump: Boolean = true
    override fun extraAccessInvariantsForParameter(): List<TypeInvariantEmbedding> = listOf(NonNegativeSizeTypeInvariantEmbedding)

    object NonNegativeSizeTypeInvariantEmbedding : TypeInvariantEmbedding {
        override fun fillHole(exp: ExpEmbedding): ExpEmbedding =
            GeCmp(FieldAccess(exp, ListSizeFieldEmbedding), IntLit(0))
    }
}

fun ScopedKotlinName.specialEmbedding(): FieldEmbedding? =
    NameMatcher.match(this) {
        ifIsCollectionInterface {
            ifMemberName("size") {
                return ListSizeFieldEmbedding
            }
        }
        return null
    }