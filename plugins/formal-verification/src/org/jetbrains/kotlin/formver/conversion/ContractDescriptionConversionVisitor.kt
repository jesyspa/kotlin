/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.contracts.description.*
import org.jetbrains.kotlin.fir.contracts.description.ConeContractConstantValues
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.formver.scala.silicon.ast.Exp
import org.jetbrains.kotlin.formver.scala.silicon.ast.Type

class ContractDescriptionConversionVisitor : KtContractDescriptionVisitor<Exp, StmtConversionContext, ConeKotlinType, ConeDiagnostic>() {
    override fun visitConstantDescriptor(
        constantReference: KtConstantReference<ConeKotlinType, ConeDiagnostic>,
        data: StmtConversionContext
    ): Exp {
        val retType = data.methodCtx.returnVar.type.viperType
        return when (constantReference) {
            ConeContractConstantValues.WILDCARD -> Exp.BoolLit(true)
            ConeContractConstantValues.NULL -> Exp.EqCmp(Exp.LocalVar("ret\$", retType), Exp.NullLit())
            ConeContractConstantValues.NOT_NULL -> Exp.NeCmp(Exp.LocalVar("ret\$", retType), Exp.NullLit())
            ConeContractConstantValues.TRUE -> Exp.EqCmp(Exp.LocalVar("ret\$", retType), Exp.BoolLit(true))
            ConeContractConstantValues.FALSE -> Exp.EqCmp(Exp.LocalVar("ret\$", retType), Exp.BoolLit(false))
            else -> throw Exception("Unexpected constant: $constantReference")
        }
    }

    override fun visitReturnsEffectDeclaration(
        returnsEffect: KtReturnsEffectDeclaration<ConeKotlinType, ConeDiagnostic>,
        data: StmtConversionContext
    ): Exp {
        return returnsEffect.value.accept(this, data)
    }

    override fun visitBooleanValueParameterReference(
        booleanValueParameterReference: KtBooleanValueParameterReference<ConeKotlinType, ConeDiagnostic>,
        data: StmtConversionContext
    ): Exp {
        // TODO: find a better way to do that
        return Exp.LocalVar("local\$${booleanValueParameterReference.name}", Type.Bool)
    }

    override fun visitLogicalBinaryOperationContractExpression(
        binaryLogicExpression: KtBinaryLogicExpression<ConeKotlinType, ConeDiagnostic>,
        data: StmtConversionContext
    ): Exp {
        val left = binaryLogicExpression.left.accept(this, data)
        val right = binaryLogicExpression.right.accept(this, data)
        return when (binaryLogicExpression.kind) {
            LogicOperationKind.AND -> Exp.And(left, right)
            LogicOperationKind.OR -> Exp.Or(left, right)
        }
    }

    override fun visitConditionalEffectDeclaration(
        conditionalEffect: KtConditionalEffectDeclaration<ConeKotlinType, ConeDiagnostic>,
        data: StmtConversionContext
    ): Exp {
        val effect = conditionalEffect.effect.accept(this, data)
        val cond = conditionalEffect.condition.accept(this, data)
        return Exp.Implies(effect, cond)
    }

    override fun visitEffectDeclaration(
        effectDeclaration: KtEffectDeclaration<ConeKotlinType, ConeDiagnostic>,
        data: StmtConversionContext
    ): Exp {
        return effectDeclaration.accept(this, data)
    }

}