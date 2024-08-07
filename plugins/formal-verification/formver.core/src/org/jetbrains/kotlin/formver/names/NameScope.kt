/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

import org.jetbrains.kotlin.formver.viper.mangled
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.utils.addToStdlib.ifFalse

sealed interface NameScope {
    val parent: NameScope?
    val mangledScopeName: String?
}

// Includes the scope itself.
val NameScope.parentScopes: Sequence<NameScope>
    get() = sequence {
        parent?.parentScopes?.let { yieldAll(it) }
        yield(this@parentScopes)
    }

val NameScope.fullMangledName: String?
    get() {
        val scopes = parentScopes.mapNotNull { it.mangledScopeName }.toList()
        return if (scopes.isEmpty()) null else scopes.joinToString("$")
    }

val NameScope.packageNameIfAny: FqName?
    get() = parentScopes.filterIsInstance<PackageScope>().lastOrNull()?.packageName

val NameScope.classNameIfAny: ClassKotlinName?
    get() = parentScopes.filterIsInstance<ClassScope>().lastOrNull()?.className

data class PackageScope(val packageName: FqName) : NameScope {
    override val parent = null

    override val mangledScopeName: String?
        get() = packageName.isRoot.ifFalse { "pkg\$${packageName.asViperString()}" }
}

data class ClassScope(override val parent: NameScope, val className: ClassKotlinName) : NameScope {
    override val mangledScopeName: String
        get() = className.mangled
}

data class PublicScope(override val parent: NameScope) : NameScope {
    override val mangledScopeName: String
        get() = "public"
}

data class PrivateScope(override val parent: NameScope) : NameScope {
    override val mangledScopeName: String
        get() = "private"
}

data object ParameterScope : NameScope {
    override val parent: NameScope? = null
    override val mangledScopeName: String
        get() = "p"
}

data class LocalScope(val level: Int) : NameScope {
    override val parent: NameScope? = null
    override val mangledScopeName = "l$level"
}

/**
 * Scope to use in cases when we need a scoped name, but don't actually want to introduce one.
 */
data object FakeScope : NameScope {
    override val parent: NameScope? = null
    override val mangledScopeName: String? = null
}
