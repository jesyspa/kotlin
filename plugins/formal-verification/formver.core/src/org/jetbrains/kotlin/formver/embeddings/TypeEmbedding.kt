/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.formver.domains.Injection
import org.jetbrains.kotlin.formver.domains.RuntimeTypeDomain
import org.jetbrains.kotlin.formver.embeddings.expression.debug.PlaintextLeaf
import org.jetbrains.kotlin.formver.embeddings.expression.debug.TreeView
import org.jetbrains.kotlin.formver.embeddings.types.PretypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.types.RuntimeTypeHolder
import org.jetbrains.kotlin.formver.names.*
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.mangled

/**
 * Represents our representation of a Kotlin type.
 *
 * Due to name mangling, the mapping between Kotlin types and TypeEmbeddings must be 1:1.
 *
 * All type embeddings must be `data` classes or objects!
 */
data class TypeEmbedding(val pretype: PretypeEmbedding, val flags: TypeEmbeddingFlags) : RuntimeTypeHolder, TypeInvariantHolder {
    /**
     * Name representing the type, used for distinguishing overloads.
     *
     * It may at some point necessary to make a `TypeName` hierarchy of some sort to
     * represent these names, but we do it inline for now.
     */
    val name: MangledName
        get() = TypeName(pretype, flags.nullable)

    /**
     * Get a nullable version of this type embedding.
     *
     * Note that nullability doesn't stack, hence nullable types must return themselves.
     */
    fun getNullable(): TypeEmbedding = copy(flags = flags.copy(nullable = true))

    /**
     * Drop nullability, if it is present.
     */
    fun getNonNullable(): TypeEmbedding = copy(flags = flags.copy(nullable = false))

    val isNullable: Boolean
        get() = flags.nullable

    override val runtimeType: Exp = flags.adjustRuntimeType(pretype.runtimeType)

    override fun accessInvariants(): List<TypeInvariantEmbedding> = flags.adjustManyInvariants(pretype.accessInvariants())
    override fun pureInvariants(): List<TypeInvariantEmbedding> = flags.adjustManyInvariants(pretype.pureInvariants())

    override fun sharedPredicateAccessInvariant(): TypeInvariantEmbedding? =
        flags.adjustOptionalInvariant(pretype.sharedPredicateAccessInvariant())

    override fun uniquePredicateAccessInvariant(): TypeInvariantEmbedding? =
        flags.adjustOptionalInvariant(pretype.uniquePredicateAccessInvariant())

    override fun subTypeInvariant(): TypeInvariantEmbedding =
        flags.adjustInvariant(pretype.subTypeInvariant())

    override val debugTreeView: TreeView
        get() = PlaintextLeaf(name.mangled)
}

data object UnitTypeEmbedding : PretypeEmbedding {
    override val runtimeType = RuntimeTypeDomain.unitType()
    override val name = PretypeName("Unit")
}

data object NothingTypeEmbedding : PretypeEmbedding {
    override val runtimeType = RuntimeTypeDomain.nothingType()
    override val name = PretypeName("Nothing")

    override fun pureInvariants(): List<TypeInvariantEmbedding> = listOf(FalseTypeInvariant)
}

data object AnyTypeEmbedding : PretypeEmbedding {
    override val runtimeType = RuntimeTypeDomain.anyType()
    override val name = PretypeName("Any")
}

data object IntTypeEmbedding : PretypeEmbedding {
    override val runtimeType = RuntimeTypeDomain.intType()
    override val name = PretypeName("Int")
}

data object BooleanTypeEmbedding : PretypeEmbedding {
    override val runtimeType = RuntimeTypeDomain.boolType()
    override val name = PretypeName("Boolean")
}

/*
// this is here to make merges easier; remove before PR
data class NullableTypeEmbedding(val elementType: TypeEmbedding) : TypeEmbedding {
    override val runtimeType = RuntimeTypeDomain.nullable(elementType.runtimeType)
    override val name = object : MangledName {
        override val mangledScope: String?
            get() = elementType.name.mangledScope?.let { "N$it" }
        override val mangledBaseName: String
            get() = elementType.name.mangledBaseName
    }

    override fun accessInvariants(): List<TypeInvariantEmbedding> = elementType.accessInvariants().map { IfNonNullInvariant(it) }
    override fun pureInvariants(): List<TypeInvariantEmbedding> = elementType.pureInvariants().map { IfNonNullInvariant(it) }

    // Note: this function will replace accessInvariants when nested unfold will be implemented
    override fun sharedPredicateAccessInvariant(): TypeInvariantEmbedding? =
        elementType.sharedPredicateAccessInvariant()?.let { IfNonNullInvariant(it) }

    override fun uniquePredicateAccessInvariant(): TypeInvariantEmbedding? =
        elementType.uniquePredicateAccessInvariant()?.let { IfNonNullInvariant(it) }

    override fun getNullable(): NullableTypeEmbedding = this
    override fun getNonNullable(): TypeEmbedding = elementType

    override val isNullable = true
}
 */

data class FunctionTypeEmbedding(
    val receiverType: TypeEmbedding?,
    val paramTypes: List<TypeEmbedding>,
    val returnType: TypeEmbedding,
    val returnsUnique: Boolean,
) : PretypeEmbedding {
    override val runtimeType = RuntimeTypeDomain.functionType()
    override val name = PretypeName(formalArgTypes.joinToString("$") { it.name.mangled })

    /**
     * The flattened structure of the callable parameters: in case the callable has a receiver
     * it becomes the first argument of the function.
     *
     * `Foo.(Int) -> Int --> (Foo, Int) -> Int`
     */
    val formalArgTypes: List<TypeEmbedding>
        get() = listOfNotNull(receiverType) + paramTypes
}

// TODO: incorporate generic parameters.
data class ClassTypeEmbedding(override val name: ScopedKotlinName) : PretypeEmbedding {
    private var _details: ClassEmbeddingDetails? = null
    val details: ClassEmbeddingDetails
        get() = _details ?: error("Details of $name have not been initialised yet.")

    fun initDetails(details: ClassEmbeddingDetails) {
        require(_details == null) { "Class details already initialized" }
        _details = details
    }

    val hasDetails: Boolean
        get() = _details != null

    // override val name = className

    val runtimeTypeFunc = RuntimeTypeDomain.classTypeFunc(name)
    override val runtimeType: Exp = runtimeTypeFunc()

    override fun accessInvariants(): List<TypeInvariantEmbedding> = details.accessInvariants()

    // Note: this function will replace accessInvariants when nested unfold will be implemented
    override fun sharedPredicateAccessInvariant() = details.sharedPredicateAccessInvariant()

    override fun uniquePredicateAccessInvariant() = details.uniquePredicateAccessInvariant()
}

data class TypeEmbeddingFlags(val nullable: Boolean) {
    fun adjustRuntimeType(runtimeType: Exp): Exp =
        if (nullable) RuntimeTypeDomain.nullable(runtimeType)
        else runtimeType

    fun adjustInvariant(invariant: TypeInvariantEmbedding): TypeInvariantEmbedding =
        if (nullable) IfNonNullInvariant(invariant)
        else invariant

    fun adjustManyInvariants(invariants: List<TypeInvariantEmbedding>): List<TypeInvariantEmbedding> =
        invariants.map { adjustInvariant(it) }

    fun adjustOptionalInvariant(invariant: TypeInvariantEmbedding?): TypeInvariantEmbedding? =
        invariant?.let { adjustInvariant(it) }
}

fun PretypeEmbedding.asTypeEmbedding() = TypeEmbedding(this, TypeEmbeddingFlags(false))

inline fun PretypeEmbedding.injectionOr(default: () -> Injection): Injection = when (this) {
    IntTypeEmbedding -> RuntimeTypeDomain.intInjection
    BooleanTypeEmbedding -> RuntimeTypeDomain.boolInjection
    else -> default()
}

private fun PretypeEmbedding.isCollectionTypeNamed(name: String): Boolean {
    val classEmbedding = this as? ClassTypeEmbedding ?: return false
    NameMatcher.matchGlobalScope(classEmbedding.name) {
        ifInCollectionsPkg {
            ifClassName(name) {
                return true
            }
        }
        return false
    }
}

fun PretypeEmbedding.isInheritorOfCollectionTypeNamed(name: String): Boolean {
    val classEmbedding = this as? ClassTypeEmbedding ?: return false
    return isCollectionTypeNamed(name) || classEmbedding.details.superTypes.any {
        it.isInheritorOfCollectionTypeNamed(name)
    }
}

val PretypeEmbedding.isCollectionInheritor
    get() = isInheritorOfCollectionTypeNamed("Collection")
