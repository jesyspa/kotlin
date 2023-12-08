/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.viper.errors

import org.jetbrains.kotlin.formver.viper.ast.Node
import org.jetbrains.kotlin.formver.viper.ast.Position
import org.jetbrains.kotlin.formver.viper.ast.info
import org.jetbrains.kotlin.formver.viper.ast.unwrapOr

class VerificationError private constructor(
    val result: viper.silver.verifier.VerificationError,
) : VerifierError {
    companion object {
        fun fromSilver(result: viper.silicon.interfaces.VerificationResult): VerificationError {
            check(result.isFatal) { "The verification result must contain an error to be converted." }
            return VerificationError((result as viper.silicon.interfaces.Failure).message())
        }
    }

    /**
     * The [offendingNode] represents the node in Viper's AST where the error occurred.
     * This is useful with error reporting, when we want to fetch missing information from the error,
     * and we need to reconstruct the original Kotlin's code context.
     */
    val offendingNode: Node
        get() = Node(result.offendingNode())

    /**
     * The [reasonOffendingNode] represents the reason error node in Viper's AST.
     * To understand better the role of this field, let us consider a simple example:
     * - we call a Viper's method with defined pre-conditions.
     * - Viper throws a [VerificationError] due to a pre-condition that is not holding.
     * - the [offendingNode] is located at the method call site, while
     * - the [reasonOffendingNode] is located on the failing pre-condition.
     */
    val reasonOffendingNode: Node
        get() = Node(result.reason().offendingNode())
    override val id: String
        get() = result.id()
    override val msg: String
        get() = result.readableMessage(false, false)
    override val position: Position
        get() = Position.fromSilver(result.pos())
}

/**
 * Given a verification error, find embedded extra information of type `I` in the
 * error's offending nodes.
 * If the extra info is not found, return `null`.
 * The information can be embedded either in the result's offending node,
 * or in the reason's offending node.
 * As an example, `PreconditionInCallFalse` errors have
 * as offending node result the call-site of the called method.
 * But the actual info we are interested in is on the pre-condition, contained in the reason's offending node.
 */
fun <I> VerificationError.getInfoOrNull(): I? =
    offendingNode.info.unwrapOr<I> {
        reasonOffendingNode.info.unwrapOr<I> { null }
    }
