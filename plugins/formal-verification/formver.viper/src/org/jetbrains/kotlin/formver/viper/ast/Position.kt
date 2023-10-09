/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.viper.ast

import org.jetbrains.kotlin.formver.viper.IntoSilver
import viper.silver.ast.`NoPosition$`

interface PositionWrapper<P> : viper.silver.ast.Position {
    val ktPosition: P
}

sealed class Position : IntoSilver<viper.silver.ast.Position> {

    data object NoPosition : Position() {
        override fun toSilver(): viper.silver.ast.Position = `NoPosition$`.`MODULE$`
    }

    data class KtSourcePosition<P>(val pos: P) : Position() {
        override fun toSilver(): viper.silver.ast.Position = object : PositionWrapper<P> {
            override fun toString(): String = pos.toString()
            override val ktPosition: P
                get() = pos
        }
    }
}

