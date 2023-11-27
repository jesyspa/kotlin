/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.viper.errors

import org.jetbrains.kotlin.formver.viper.ast.Info
import org.jetbrains.kotlin.formver.viper.ast.Position
import org.jetbrains.kotlin.formver.viper.ast.info
import org.jetbrains.kotlin.formver.viper.ast.unwrapOr
import scala.collection.Seq
import viper.silver.ast.Exp

/**
 * This class acts as wrapper for Viper's [viper.silver.verifier.ErrorReason].
 * This is necessary since extension functions on [viper.silver.verifier.ErrorReason] cannot be
 * used outside the class' package.
 */
data class ErrorReason(val reason: viper.silver.verifier.ErrorReason)

class VerificationError private constructor(
    val result: viper.silver.verifier.VerificationError
) : VerifierError {
    companion object {
        fun fromSilver(result: viper.silicon.interfaces.VerificationResult): VerificationError {
            check(result.isFatal) { "The verification result must contain an error to be converted." }
            return VerificationError((result as viper.silicon.interfaces.Failure).message())
        }
    }

    val reason: ErrorReason
        get() = ErrorReason(result.reason())
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

/**
 * Extract the[Info] metadata from a [viper.silver.ast.MethodCall] argument.
 */
fun VerificationError.extractInfoFromFunctionArgument(argIndex: Int, isFromReason: Boolean = false): Info =
    extraInfoFromCallableArgument<viper.silver.ast.FuncApp>(argIndex, isFromReason) { args() }

/**
 * Extract the [Info] metadata from the `argIndex`-th argument of a callable.
 * @param argIndex The n-th callable argument from where extract the metadata.
 * @param isFromReason If the info metadata is contained in the error's reason offending node.
 * @param body Lambda function defining how to access the callable's argument list.
 */
private inline fun <reified T> VerificationError.extraInfoFromCallableArgument(
    argIndex: Int,
    isFromReason: Boolean,
    body: T.() -> Seq<Exp>,
): Info {
    val offendingNode = if (isFromReason) {
        reason.reason.offendingNode()
    } else {
        result.offendingNode()
    }
    return when (offendingNode) {
        is T -> {
            val arg = body(offendingNode).apply(argIndex)
            Info.fromSilver(arg.info)
        }
        else -> error("The node is not of type ${T::class}.")
    }
}