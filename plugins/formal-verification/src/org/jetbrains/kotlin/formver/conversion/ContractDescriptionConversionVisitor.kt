/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.contracts.description.KtContractDescriptionVisitor
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.formver.scala.silicon.ast.Exp

class ContractDescriptionConversionVisitor : KtContractDescriptionVisitor<Exp, StmtConversionContext, ConeKotlinType, ConeDiagnostic>() {
}