/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.reporting

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1

data class HumanReadableMessage(val pluginError: KtDiagnosticFactory1<String>, val extraMsg: String? = null)