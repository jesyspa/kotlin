/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.reporting.style

import org.jetbrains.kotlin.formver.reporting.HumanReadableMessage
import org.jetbrains.kotlin.formver.viper.errors.ConsistencyError
import org.jetbrains.kotlin.formver.viper.errors.VerificationError
import org.jetbrains.kotlin.formver.viper.errors.VerifierError

interface ErrorStyleStrategy {
    fun convert(verifyError: VerifierError): List<HumanReadableMessage> = when (verifyError) {
        is ConsistencyError -> convert(verifyError)
        is VerificationError -> convert(verifyError)
    }

    fun convert(consistencyError: ConsistencyError): List<HumanReadableMessage>
    fun convert(verificationError: VerificationError): List<HumanReadableMessage>
}