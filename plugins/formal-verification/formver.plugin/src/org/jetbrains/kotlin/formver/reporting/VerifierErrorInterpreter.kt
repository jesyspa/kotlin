/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.reporting

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.formver.ErrorStyle
import org.jetbrains.kotlin.formver.PluginErrors
import org.jetbrains.kotlin.formver.embeddings.SourceRole
import org.jetbrains.kotlin.formver.viper.errors.ConsistencyError
import org.jetbrains.kotlin.formver.viper.errors.VerificationError
import org.jetbrains.kotlin.formver.viper.errors.VerifierError
import org.jetbrains.kotlin.formver.viper.errors.getInfoOrNull

object VerifierErrorInterpreter {

    private fun fetchVerificationErrorReporter(error: VerificationError): ErrorReporter<*>? =
        when (val role = error.getInfoOrNull<SourceRole>()) {
            is SourceRole.ReturnsEffect -> ReturnsEffectReporter(error, role)
            is SourceRole.CallsInPlaceEffect -> CallsInPlaceReporter(error, role)
            is SourceRole.ParamFunctionLeakageCheck -> LeakingLambdaReporter(error, role)
            is SourceRole.ConditionalEffect -> ConditionalEffectReporter(error, role)
            else -> null
        }

    private fun DiagnosticReporter.reportOriginalVerificationError(
        source: KtSourceElement?,
        error: VerificationError,
        context: CheckerContext,
    ) {
        reportOn(source, PluginErrors.VIPER_VERIFICATION_ERROR, error.msg, context)
    }

    private fun DiagnosticReporter.reportVerificationError(
        source: KtSourceElement?,
        error: VerificationError,
        errorStyle: ErrorStyle,
        context: CheckerContext,
    ) {
        when (errorStyle) {
            ErrorStyle.USER_FRIENDLY -> {
                val reporter = fetchVerificationErrorReporter(error)
                // If we do not have a human-friendly error report, fallback to the original error message from Viper.
                reporter?.report(this, source, context) ?: reportOriginalVerificationError(source, error, context)
            }
            ErrorStyle.ORIGINAL_VIPER -> {
                reportOriginalVerificationError(source, error, context)
            }
            ErrorStyle.BOTH -> {
                val reporter = fetchVerificationErrorReporter(error)
                reporter?.report(this, source, context)
                reportOriginalVerificationError(source, error, context)
            }
        }
    }

    private fun DiagnosticReporter.reportConsistencyError(source: KtSourceElement?, error: ConsistencyError, context: CheckerContext) {
        val sourceIsFunctionDeclaration = source?.elementType?.let { it == KtNodeTypes.FUN } ?: false
        val positionStrategy = when (sourceIsFunctionDeclaration) {
            true -> SourceElementPositioningStrategies.DECLARATION_NAME
            false -> SourceElementPositioningStrategies.DEFAULT
        }
        reportOn(source, PluginErrors.INTERNAL_ERROR, error.msg, context, positioningStrategy = positionStrategy)
    }

    fun DiagnosticReporter.reportVerifierError(
        source: KtSourceElement?,
        error: VerifierError,
        errorStyle: ErrorStyle,
        context: CheckerContext,
    ) = when (error) {
        is ConsistencyError -> reportConsistencyError(source, error, context)
        is VerificationError -> reportVerificationError(source, error, errorStyle, context)
    }
}