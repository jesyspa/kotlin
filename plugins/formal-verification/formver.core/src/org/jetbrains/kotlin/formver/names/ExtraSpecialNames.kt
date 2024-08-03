/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

import org.jetbrains.kotlin.name.Name

object ExtraSpecialNames {
    @JvmField
    val E_THIS = Name.special("<E_THIS>")

    @JvmField
    val D_THIS = Name.special("<D_THIS>")
}