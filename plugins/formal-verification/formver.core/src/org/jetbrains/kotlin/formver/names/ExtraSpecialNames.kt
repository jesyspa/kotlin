/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

import org.jetbrains.kotlin.name.Name

/*
 * Those are actual `Name`s from Kotlin.
 * The structure of the object is the same as in `SpecialNames`.
 * They never end up in Viper output and are meant for internal use.
 */
object ExtraSpecialNames {
    @JvmField
    val EXTENSION_THIS = Name.special("<E_THIS>")

    @JvmField
    val DISPATCH_THIS = Name.special("<D_THIS>")
}