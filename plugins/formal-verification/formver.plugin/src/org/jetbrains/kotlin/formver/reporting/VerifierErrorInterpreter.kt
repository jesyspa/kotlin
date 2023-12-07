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

private fun fetchVerificationErrorReporter(error: VerificationError): ErrorReporter =
    when (val sourceRole = error.getInfoOrNull<SourceRole>()) {
        is SourceRole.ReturnsEffect -> ReturnsEffectReporter(sourceRole)
        is SourceRole.CallsInPlaceEffect -> CallsInPlaceReporter(sourceRole)
        is SourceRole.ParamFunctionLeakageCheck -> LeakingLambdaReporter(error)
        is SourceRole.ConditionalEffect -> ConditionalEffectReporter(sourceRole)
        else -> DefaultErrorReporter(error)
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
            reporter.report(this, source, context)
        }
        ErrorStyle.ORIGINAL_VIPER -> {
            DefaultErrorReporter(error).report(this, source, context)
        }
        ErrorStyle.BOTH -> {
            val reporter = fetchVerificationErrorReporter(error)
            reporter.report(this, source, context)
            // Avoid duplicate warnings if we do not have a specific reporter for the error.
            if (reporter !is DefaultErrorReporter) {
                DefaultErrorReporter(error).report(this, source, context)
            }
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