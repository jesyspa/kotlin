/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.reporting

import org.jetbrains.kotlin.contracts.description.EventOccurrencesRange
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.renderReadable
import org.jetbrains.kotlin.formver.embeddings.SourceRole

/**
 * Extends a given SourceRole type with a pretty printed message for reporting user-friendly messages.
 */
interface SourceRolePrettyPrinter<T : SourceRole> {
    fun T.prettyPrint(): String
}

object ReturnsEffectPrinter : SourceRolePrettyPrinter<SourceRole.ReturnsEffect> {
    override fun SourceRole.ReturnsEffect.prettyPrint(): String = when (this) {
        is SourceRole.ReturnsEffect.Bool -> if (bool) "false" else "true"
        is SourceRole.ReturnsEffect.Null -> if (negated) "null" else "non-null"
        else -> error("Unreachable")
    }
}

object CallsInPlacePrinter : SourceRolePrettyPrinter<SourceRole.CallsInPlaceEffect> {
    override fun SourceRole.CallsInPlaceEffect.prettyPrint(): String = when (kind) {
        EventOccurrencesRange.AT_MOST_ONCE -> "at most once"
        EventOccurrencesRange.EXACTLY_ONCE -> "exactly once"
        EventOccurrencesRange.AT_LEAST_ONCE -> "at least once"
        EventOccurrencesRange.MORE_THAN_ONCE -> "more than once"
        else -> error("Unreachable")
    }
}

object ConditionalEffectPrinter {
    object ReturnsEffect : SourceRolePrettyPrinter<SourceRole.ReturnsEffect> {
        override fun SourceRole.ReturnsEffect.prettyPrint(): String = when (this) {
            is SourceRole.ReturnsEffect.Bool, is SourceRole.ReturnsEffect.Null -> "a $this value is returned"
            SourceRole.ReturnsEffect.Wildcard -> "the function returns"
        }
    }

    object Condition : SourceRolePrettyPrinter<SourceRole.Condition> {
        private val ConeKotlinType.rendered: String
            get() = renderReadable()

        private val FirBasedSymbol<*>.rendered: String
            get() = FirDiagnosticRenderers.DECLARATION_NAME.render(this)

        override fun SourceRole.Condition.prettyPrint(): String = when (this) {
            is SourceRole.FirSymbolHolder -> firSymbol.rendered
            is SourceRole.Condition.Constant -> literal.toString()
            is SourceRole.Condition.Negation -> "!${arg.prettyPrint()}"
            is SourceRole.Condition.Conjunction -> "(${lhs.prettyPrint()} && ${rhs.prettyPrint()})"
            is SourceRole.Condition.Disjunction -> "(${lhs.prettyPrint()} || ${rhs.prettyPrint()})"
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
    }
}
