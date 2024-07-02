/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

import org.jetbrains.kotlin.name.FqName

data class PackageNamespace(val packageName: FqName) : ResolvableNamespace, ResolvableImplMixin() {
    override val phase: ResolutionPhase
        get() = TODO("Not yet implemented")

    override val primary: ChunkedName =
        if (packageName.isRoot) ChunkedName()
        else ChunkedName("pkg") + packageName.asChunkedName()
    override val secondary: List<ChunkedName> =
        if (packageName.isRoot) listOf()
        else listOf(packageName.asChunkedName())
}

data class ClassScope(val name: FqName) : ResolvableNamespace, ResolvableImplMixin() {
    override val phase: ResolutionPhase
        get() = TODO("Not yet implemented")

    override val primary: ChunkedName = ChunkedName("class") + name.asChunkedName()
    override val secondary: List<ChunkedName> = listOf(name.asChunkedName())
}

data class ClassPrivateScope(val name: FqName) : ResolvableNamespace, ResolvableImplMixin() {
    override val phase: ResolutionPhase
        get() = TODO("Not yet implemented")
    override val optional: Boolean = true
    override val primary: ChunkedName = ChunkedName("class") + name.asChunkedName()
    override val secondary: List<ChunkedName> = listOf(name.asChunkedName())
}

data object PublicMemberScope : ResolvableNamespace, ResolvableImplMixin() {
    override val phase: ResolutionPhase = PackagePhase
    override val optional: Boolean = true
    override val primary: ChunkedName = ChunkedName("public")
}

data object ParameterScope : ResolvableNamespace, ResolvableImplMixin() {
    override val phase: ResolutionPhase = PackagePhase
    override val primary: ChunkedName = ChunkedName("local")
}

data class LocalScope(val level: Int) : ResolvableNamespace, ResolvableImplMixin() {
    override val phase: ResolutionPhase = PackagePhase
    override val primary: ChunkedName = ChunkedName("local$level")
}
