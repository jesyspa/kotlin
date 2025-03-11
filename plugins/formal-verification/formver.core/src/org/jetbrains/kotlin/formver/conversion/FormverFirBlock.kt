/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.formver.isFormverFunctionNamed

private fun FirStatement.extractFormverFirBlock(predicate: FirFunctionSymbol<*>.() -> Boolean): FirBlock? {
    if (this !is FirFunctionCall) return null
    val firFunction = toResolvedCallableSymbol() as? FirFunctionSymbol<*> ?: return null
    if (!predicate(firFunction)) return null
    val formverInvariantsArgument = argument
    if (formverInvariantsArgument !is FirAnonymousFunctionExpression)
        error("Only lambdas are allowed as arguments of ${firFunction.name}.")
    return formverInvariantsArgument.anonymousFunction.body
}

fun extractLoopInvariants(parentBlock: FirBlock): FirBlock? {
    val firstStmt = parentBlock.statements.firstOrNull() ?: return null
    return firstStmt.extractFormverFirBlock { isFormverFunctionNamed("loopInvariants") }
}

data class FirSpecification(val precond: FirBlock?, val postcond: FirBlock?)

fun extractFirSpecification(parentBlock: FirBlock): FirSpecification {
    val firstStmt = parentBlock.statements.firstOrNull() ?: return FirSpecification(null, null)

    firstStmt.extractFormverFirBlock { isFormverFunctionNamed("postconditions") }?.let {
        return FirSpecification(null, it)
    }

    val precond = firstStmt.extractFormverFirBlock { isFormverFunctionNamed("preconditions") }
        ?: return FirSpecification(null, null)
    val postcond = parentBlock.statements.getOrNull(1)?.extractFormverFirBlock { isFormverFunctionNamed("postconditions") }
    return FirSpecification(precond, postcond)
}

