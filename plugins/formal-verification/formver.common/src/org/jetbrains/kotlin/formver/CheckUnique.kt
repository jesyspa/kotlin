/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver

import org.jetbrains.kotlin.formver.TargetsSelection.TARGETS_WITH_CONTRACT

enum class CheckUnique {
    ALWAYS_CHECK, NEVER_CHECK;

    companion object {
        @JvmStatic
        fun defaultBehaviour(): CheckUnique {
            return ALWAYS_CHECK
        }
    }
}