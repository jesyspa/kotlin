/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.formver.asPosition
import org.jetbrains.kotlin.formver.embeddings.expression.ExpEmbedding
import org.jetbrains.kotlin.formver.linearization.LinearizationContext
import org.jetbrains.kotlin.formver.linearization.pureToViper
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.Label
import org.jetbrains.kotlin.formver.viper.ast.Stmt

data class LabelLink(val name: MangledName)

data class LabelEmbedding(val labelLink: LabelLink, val invariants: List<ExpEmbedding> = emptyList()) {
    val name: MangledName
        get() = labelLink.name
}

fun LabelLink.toLabel(invariants: List<ExpEmbedding> = emptyList()): LabelEmbedding = LabelEmbedding(this, invariants)

fun LabelLink.toViperGoto(ctx: LinearizationContext): Stmt.Goto = Label(name, emptyList()).toGoto(pos = ctx.source.asPosition)

fun LabelEmbedding.toViper(ctx: LinearizationContext): Label {
    return Label(name, invariants.pureToViper(toBuiltin = true, ctx.source))
}
