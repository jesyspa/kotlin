/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.viper.ast

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.formver.viper.IntoSilver
import viper.silver.ast.`NoPosition$`
import java.nio.file.Path

interface KtSourcePosition : viper.silver.ast.Position {
    val pos: KtSourceElement
}

sealed class Position : IntoSilver<viper.silver.ast.Position> {
    data object NoPosition : Position() {
        override fun toSilver(): viper.silver.ast.Position = `NoPosition$`.`MODULE$`
    }
    data class SourcePosition(val file: Path, val line: Int, val column: Int) : Position() {
        override fun toSilver(): viper.silver.ast.Position = viper.silver.ast.SourcePosition.apply(file, line, column)
    }
}

