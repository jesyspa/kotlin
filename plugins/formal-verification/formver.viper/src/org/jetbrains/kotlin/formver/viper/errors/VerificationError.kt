/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.viper.errors

import org.jetbrains.kotlin.formver.viper.ast.*

class VerificationError private constructor(
    val result: viper.silver.verifier.VerificationError,
) : VerifierError {
    companion object {
        fun fromSilver(result: viper.silicon.interfaces.VerificationResult): VerificationError {
            check(result.isFatal) { "The verification result must contain an error to be converted." }
            return VerificationError((result as viper.silicon.interfaces.Failure).message())
        }
    }

    val offendingNode: Node
        get() = Node(result.offendingNode())
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
    Info.fromSilver(result.offendingNode().info).unwrapOr<I> {
        Info.fromSilver(result.reason().offendingNode().info).unwrapOr<I> { null }
    }
