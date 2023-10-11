/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.viper.ast

import org.jetbrains.kotlin.formver.viper.IntoSilver
import viper.silver.ast.`NoPosition$`

data class PositionWrapper<P>(val ktPosition: P) : viper.silver.ast.Position

sealed class Position : IntoSilver<viper.silver.ast.Position> {

    companion object {
        fun fromSilver(pos: viper.silver.ast.Position): Position = when {
            pos == `NoPosition$`.`MODULE$` -> NoPosition
            else -> {
                KtSourcePosition((pos as PositionWrapper<*>).ktPosition)
            }
        }
    }

    data object NoPosition : Position() {
        override fun toSilver(): viper.silver.ast.Position = `NoPosition$`.`MODULE$`
    }

    data class KtSourcePosition<P>(val wrappedPosition: P) : Position() {
        override fun toSilver(): viper.silver.ast.Position = PositionWrapper(wrappedPosition)
    }
}

//region Define Extension Function Utilities
inline fun <reified P> Position.unwrap(): P? = when (this) {
    is Position.KtSourcePosition<*> -> wrappedPosition as? P
    else -> null
}

fun Position.isEmpty(): Boolean = (this is Position.NoPosition)
//endregion