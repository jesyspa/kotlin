/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver

import org.jetbrains.kotlin.formver.info.SourceRole
import org.jetbrains.kotlin.formver.viper.ast.Info

/***
 * This file contains extension properties and functions not related to FIR elements.
 */

val SourceRole.asInfo: Info
    get() = Info.Wrapped(this)