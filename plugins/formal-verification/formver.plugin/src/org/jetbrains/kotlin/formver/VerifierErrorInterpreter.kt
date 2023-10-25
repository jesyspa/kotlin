/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver

import org.jetbrains.kotlin.formver.info.ContextInfo
import org.jetbrains.kotlin.formver.viper.errors.*

/**
 * TODO: currently the error messages are returned as plain string, not using the `PluginErrors` object.
 *       We have to investigate if it is better to use `VIPER_VERIFICATION_ERROR` only,
 *       or creating an error for each possible type.
 */

object VerifierErrorInterpreter {
    private fun ConsistencyError.toHumanReadableMessage(debug: Boolean): String = when (debug) {
        true -> this.msg
        false -> "An internal error has occurred while verifying the function. Please report the issue."
    }

    private fun VerificationError.toHumanReadableMessage(debug: Boolean): String {
        val context = getInfoOr<ContextInfo>(ContextInfo.Unknown)
        return when {
            this is PostconditionViolated && context is ContextInfo.CondNullEffect -> "the following nullability condition might not hold."
            this is PostconditionViolated && context is ContextInfo.CondNotNullEffect -> "the following non-nullability condition might not hold."
            else -> if (debug) this.msg else "No further information is available for this warning."
        }
    }

    fun VerifierError.toHumanReadableMessage(debug: Boolean = true): String = when (this) {
        is ConsistencyError -> toHumanReadableMessage(debug)
        is VerificationError -> toHumanReadableMessage(debug)
        else -> TODO("Unreachable")
    }
}