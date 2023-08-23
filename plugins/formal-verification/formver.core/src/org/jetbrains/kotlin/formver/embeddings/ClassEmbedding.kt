/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.formver.scala.silicon.ast.Type

class ClassEmbedding(
    val name: ClassName,
    val fields: MutableList<VariableEmbedding>,
    val methods: MutableList<MethodSignatureEmbedding>
) : TypeEmbedding {

    override val type: Type
        get() = Type.Ref
}