/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.info

sealed class SourceRole {
    data object Unknown : SourceRole()
    data object ReturnsEffect : SourceRole()
    data object ReturnsTrueEffect : SourceRole()
    data object ReturnsFalseEffect : SourceRole()
    data object CondNullEffect : SourceRole()
    data object CondNotNullEffect : SourceRole()
}
