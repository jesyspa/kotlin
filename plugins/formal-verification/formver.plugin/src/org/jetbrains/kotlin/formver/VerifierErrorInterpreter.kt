/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.formver.viper.errors.*
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

data class HumanReadableMessage(val pluginError: KtDiagnosticFactory1<String>, val extraMsg: String? = null)

class VerifierErrorInterpreter {

    fun DiagnosticReporter.reportVerifierError(source: KtSourceElement?, error: VerifierError, context: CheckerContext) {
        val (diagnosticFactory, message) = error.toHumanReadableMessage()
        reportOn(source, diagnosticFactory, message ?: "", context)
    }

    private fun ConsistencyError.toHumanReadableMessage(debug: Boolean): HumanReadableMessage = when (debug) {
        true -> HumanReadableMessage(PluginErrors.VIPER_CONSISTENCY_ERROR, this.msg)
        false -> HumanReadableMessage(PluginErrors.REPORT_CONSISTENCY_ERROR)
    }

    private fun VerificationError.toHumanReadableMessage(debug: Boolean): HumanReadableMessage {
        val role = getInfoOrNull<SourceRole>()
        return when {
            this is PostconditionViolated && role is SourceRole.ReturnsTrueEffect ->
                HumanReadableMessage(PluginErrors.UNEXPECTED_RETURNED_VALUE, "false")
            this is PostconditionViolated && role is SourceRole.ReturnsFalseEffect ->
                HumanReadableMessage(PluginErrors.UNEXPECTED_RETURNED_VALUE, "true")
            this is PostconditionViolated && role is SourceRole.ReturnsNullEffect ->
                HumanReadableMessage(PluginErrors.UNEXPECTED_RETURNED_VALUE, "non-null")
            this is PostconditionViolated && role is SourceRole.ReturnsNotNullEffect ->
                HumanReadableMessage(PluginErrors.UNEXPECTED_RETURNED_VALUE, "null")
            else -> HumanReadableMessage(PluginErrors.VIPER_VERIFICATION_ERROR, debug.ifTrue { msg })
        }
    }

    fun VerifierError.toHumanReadableMessage(debug: Boolean = true): HumanReadableMessage = when (this) {
        is ConsistencyError -> toHumanReadableMessage(debug)
        is VerificationError -> toHumanReadableMessage(debug)
        else -> TODO("Unreachable")
    }
}