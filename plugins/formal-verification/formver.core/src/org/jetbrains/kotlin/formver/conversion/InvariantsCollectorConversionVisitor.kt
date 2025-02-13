/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.types.isBoolean
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.formver.embeddings.expression.ExpEmbedding

interface InvariantsCollectorConversionContext : StmtConversionContext {
    fun addInvariant(exp: ExpEmbedding)
    val invariants: List<ExpEmbedding>
}

class InvariantsCollectorConverter(private val stmtCtx: StmtConversionContext) : StmtConversionContext by stmtCtx,
    InvariantsCollectorConversionContext {

    override fun addInvariant(exp: ExpEmbedding) {
        _invariants.add(exp)
    }

    private val _invariants = mutableListOf<ExpEmbedding>()
    override val invariants: List<ExpEmbedding>
        get() = _invariants
}

object InvariantsCollectorConversionVisitor : FirVisitor<Unit, InvariantsCollectorConversionContext>() {
    private const val INVALID_STATEMENT_MSG =
        "Every statement in `loopInvariants` must be a pure boolean invariant."

    override fun visitElement(element: FirElement, data: InvariantsCollectorConversionContext) =
        error("`LoopInvariantsConversionVisitor` must be used to convert `FirBlock`s only.")

    override fun visitBlock(block: FirBlock, data: InvariantsCollectorConversionContext) {
        block.statements.forEach { stmt ->
            check(stmt is FirExpression && stmt.resolvedType.isBoolean) {
                INVALID_STATEMENT_MSG
            }
            data.addInvariant(stmt.accept(StmtConversionVisitor, data))
        }
    }
}