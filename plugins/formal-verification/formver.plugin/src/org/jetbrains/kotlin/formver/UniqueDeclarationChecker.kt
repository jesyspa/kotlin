/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirSimpleFunctionChecker
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.FirVariable
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

enum class UniquenessLevel {
    Unique,
    Shared
}

class UniqueDeclarationChecker(private val session: FirSession, private val config: PluginConfiguration) :
    FirSimpleFunctionChecker(MppCheckerKind.Common) {

    private fun getAnnotationId(name: String): ClassId =
        ClassId(FqName.fromSegments(listOf("org", "jetbrains", "kotlin", "formver", "plugin")), Name.identifier(name))

    private val uniqueId: ClassId = getAnnotationId("Unique")

    private fun getUniqueLevelFromExpression(expression: FirExpression, context: Map<FirVariable, UniquenessLevel>): UniquenessLevel {
        return UniquenessLevel.Shared
    }

    @OptIn(SymbolInternals::class)
    override fun check(declaration: FirSimpleFunction, context: CheckerContext, reporter: DiagnosticReporter) {
        if (config.checkUnique == CheckUnique.NEVER_CHECK) return
        val errorCollector = ErrorCollector()
        try {
            // uniquenessContext is a map from variable to its uniqueness level
            val uniquenessContext: MutableMap<FirVariable, UniquenessLevel> = mutableMapOf()
            // iterate through all parameters and checker their annotation
            declaration.valueParameters.forEach { parameter ->
                if (parameter.hasAnnotation(uniqueId, session)) {
                    uniquenessContext[parameter] = UniquenessLevel.Unique
                } else {
                    uniquenessContext[parameter] = UniquenessLevel.Shared
                }
            }

            // analyze the function body
            val body = declaration.body ?: return
            body.statements.forEach { statement: FirStatement ->
                // check if statement is a function call, otherwise, continue
                if (statement is FirFunctionCall) {
                    val functionReference = statement.calleeReference as FirResolvedNamedReference
                    val functionDeclaration = functionReference.resolvedSymbol.fir as FirSimpleFunction
                    val functionParameters = functionDeclaration.valueParameters
                    val functionArguments = statement.arguments
                    assert(functionArguments.size == functionParameters.size)
                    // TODO: Should do unify here
                    for (i in functionParameters.indices) {
                        val parameter = functionParameters[i]
                        if (!parameter.hasAnnotation(uniqueId, session)) continue
                        val argument = functionArguments[i]
                        if (getUniqueLevelFromExpression(argument, uniquenessContext) == UniquenessLevel.Shared) {
                            reporter.reportOn(argument.source, PluginErrors.UNIQUENESS_VIOLATION, parameter.name.asString(), context)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            val error = errorCollector.formatErrorWithInfos(e.message ?: "No message provided")
            reporter.reportOn(declaration.source, PluginErrors.INTERNAL_ERROR, error, context)
        }
    }
}