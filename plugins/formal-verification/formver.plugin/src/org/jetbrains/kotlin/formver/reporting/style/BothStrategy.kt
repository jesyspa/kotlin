/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.reporting.style

import org.jetbrains.kotlin.formver.reporting.HumanReadableMessage
import org.jetbrains.kotlin.formver.viper.errors.VerificationError

object BothStrategy : ErrorStyleStrategy {
    /**
     * We are only interested into `VerificationError`s since the consistency ones
     * are always the same among strategies.
     */
    override fun convert(verificationError: VerificationError): List<HumanReadableMessage> =
        UserFriendlyStrategy.convert(verificationError) + OriginalViperStrategy.convert(verificationError)
}