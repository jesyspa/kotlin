/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.reporting.style

import org.jetbrains.kotlin.formver.PluginErrors
import org.jetbrains.kotlin.formver.SourceRole
import org.jetbrains.kotlin.formver.reporting.HumanReadableMessage
import org.jetbrains.kotlin.formver.viper.errors.PostconditionViolated
import org.jetbrains.kotlin.formver.viper.errors.VerificationError
import org.jetbrains.kotlin.formver.viper.errors.getInfoOrNull

object UserFriendlyStrategy : ErrorStyleStrategy {
    override fun convert(verificationError: VerificationError): List<HumanReadableMessage> =
        listOf(verificationError.toHumanReadableMessage())

    private fun VerificationError.toHumanReadableMessage(): HumanReadableMessage {
        val role = getInfoOrNull<SourceRole>()
        return when {
            this is PostconditionViolated && role is SourceRole.ReturnsTrueEffect ->
                HumanReadableMessage(PluginErrors.UNEXPECTED_RETURNED_VALUE, "false")
            this is PostconditionViolated && role is SourceRole.ReturnsFalseEffect ->
                HumanReadableMessage(PluginErrors.UNEXPECTED_RETURNED_VALUE, "true")
            this is PostconditionViolated && role is SourceRole.ReturnsNullEffect ->
                HumanReadableMessage(PluginErrors.UNEXPECTED_RETURNED_VALUE, "non-null")
            this is PostconditionViolated && role is SourceRole.ReturnsNotNullEffect ->
                HumanReadableMessage(PluginErrors.UNEXPECTED_RETURNED_VALUE, "null")
            // Fallback in a generic VIPER_VERIFICATION_ERROR for the not handled cases.
            else -> HumanReadableMessage(PluginErrors.VIPER_VERIFICATION_ERROR)
        }
    }
}