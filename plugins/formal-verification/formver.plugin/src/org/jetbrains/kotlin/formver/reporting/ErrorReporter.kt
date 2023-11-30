/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.reporting

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.contracts.description.EventOccurrencesRange
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.renderReadable
import org.jetbrains.kotlin.formver.PluginErrors
import org.jetbrains.kotlin.formver.embeddings.SourceRole
import org.jetbrains.kotlin.formver.embeddings.SourceRole.ParamFunctionLeakageCheck.fetchLeakingFunction
import org.jetbrains.kotlin.formver.viper.errors.VerificationError

/**
 * Abstract class defining a [report] method used to warn users with human-readable messages.
 */
abstract class ErrorReporter<S : SourceRole>(
    protected val error: VerificationError,
    protected val role: S,
) {
    abstract fun report(reporter: DiagnosticReporter, source: KtSourceElement?, context: CheckerContext)
}

class ReturnsEffectReporter(error: VerificationError, source: SourceRole.ReturnsEffect) :
    ErrorReporter<SourceRole.ReturnsEffect>(error, source) {
    private val SourceRole.ReturnsEffect.asUserFriendlyMessage: String
        get() = when (this) {
            is SourceRole.ReturnsEffect.Bool -> if (bool) "false" else "true"
            is SourceRole.ReturnsEffect.Null -> if (negated) "null" else "non-null"
            else -> throw IllegalStateException("Unknown returns effect: $this")
        }

    override fun report(reporter: DiagnosticReporter, source: KtSourceElement?, context: CheckerContext) {
        reporter.reportOn(source, PluginErrors.UNEXPECTED_RETURNED_VALUE, role.asUserFriendlyMessage, context)
    }
}

class CallsInPlaceReporter(error: VerificationError, role: SourceRole.CallsInPlaceEffect) :
    ErrorReporter<SourceRole.CallsInPlaceEffect>(error, role) {
    private val SourceRole.CallsInPlaceEffect.asUserFriendlyMessage: String
        get() = when (kind) {
            EventOccurrencesRange.AT_MOST_ONCE -> "at most once"
            EventOccurrencesRange.EXACTLY_ONCE -> "exactly once"
            EventOccurrencesRange.AT_LEAST_ONCE -> "at least once"
            EventOccurrencesRange.MORE_THAN_ONCE -> "more than once"
            else -> throw IllegalStateException("Unknown kind of range: $kind")
        }

    override fun report(reporter: DiagnosticReporter, source: KtSourceElement?, context: CheckerContext) {
        reporter.reportOn(source, PluginErrors.INVALID_INVOCATION_TYPE, role.paramSymbol, role.asUserFriendlyMessage, context)
    }
}

class LeakingLambdaReporter(error: VerificationError, role: SourceRole.ParamFunctionLeakageCheck) :
    ErrorReporter<SourceRole.ParamFunctionLeakageCheck>(error, role) {
    override fun report(reporter: DiagnosticReporter, source: KtSourceElement?, context: CheckerContext) {
        reporter.reportOn(source, PluginErrors.LAMBDA_MAY_LEAK, error.fetchLeakingFunction(), context)
    }
}

class ConditionalEffectReporter(error: VerificationError, role: SourceRole.ConditionalEffect) :
    ErrorReporter<SourceRole.ConditionalEffect>(error, role) {
    private inline val ConeKotlinType.rendered: String
        get() = renderReadable()

    private inline val FirBasedSymbol<*>.rendered: String
        get() = FirDiagnosticRenderers.DECLARATION_NAME.render(this)

    private val SourceRole.ReturnsEffect.asUserFriendlyMessage: String
        get() = when (this) {
            is SourceRole.ReturnsEffect.Bool, is SourceRole.ReturnsEffect.Null -> "a $this value is returned"
            SourceRole.ReturnsEffect.Wildcard -> "the function returns"
        }

    private fun SourceRole.Condition.prettyPrint(): String = when (this) {
        is SourceRole.FirSymbolHolder -> firSymbol.rendered
        is SourceRole.Condition.Constant -> literal.toString()
        is SourceRole.Condition.Negation -> "!${arg.prettyPrint()}"
        is SourceRole.Condition.Conjunction -> {
            val lhsExpr = lhs.prettyPrint()
            val rhsExpr = rhs.prettyPrint()
            "($lhsExpr && $rhsExpr)"
        }
        is SourceRole.Condition.Disjunction -> {
            val lhsExpr = lhs.prettyPrint()
            val rhsExpr = rhs.prettyPrint()
            "($lhsExpr || $rhsExpr)"
        }
        is SourceRole.Condition.IsNull -> buildString {
            append(targetVariable.rendered)
            when (negated) {
                true -> append(" != ")
                false -> append(" == ")
            }
            append("null")
        }
        is SourceRole.Condition.IsType -> buildString {
            append(targetVariable.rendered)
            when (negated) {
                true -> append(" !is ")
                false -> append(" is ")
            }
            append(expectedType.rendered)
        }
    }

    override fun report(reporter: DiagnosticReporter, source: KtSourceElement?, context: CheckerContext) {
        val (returnEffect, condition) = role
        val returnEffectMsg = returnEffect.asUserFriendlyMessage
        val conditionPrettyPrinted = condition.prettyPrint()
        reporter.reportOn(source, PluginErrors.CONDITIONAL_EFFECT_ERROR, returnEffectMsg, conditionPrettyPrinted, context)
    }
}