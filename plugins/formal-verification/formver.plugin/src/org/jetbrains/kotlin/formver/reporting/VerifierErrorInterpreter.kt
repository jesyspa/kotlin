/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.reporting

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.formver.reporting.style.ErrorStyleStrategy
import org.jetbrains.kotlin.formver.viper.errors.VerifierError

class VerifierErrorInterpreter(
    private val errorStyleStrategy: ErrorStyleStrategy
) {
    fun DiagnosticReporter.reportVerifierError(source: KtSourceElement?, error: VerifierError, context: CheckerContext) {
        errorStyleStrategy.convert(error).forEach {
            val (diagnosticFactory, message) = it
            reportOn(source, diagnosticFactory, message ?: "", context)
        }
    }
}