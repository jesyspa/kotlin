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
import org.jetbrains.kotlin.formver.scala.silicon.ast.Exp.*
import org.jetbrains.kotlin.formver.scala.silicon.ast.Type

class ContractDescriptionConversionVisitor : KtContractDescriptionVisitor<Exp, MethodConversionContext, ConeKotlinType, ConeDiagnostic>() {
    private fun KtValueParameterReference<ConeKotlinType, ConeDiagnostic>.convertedVar(data: MethodConversionContext): ConvertedVar {
        val name = data.signature.params[parameterIndex].name
        val type = data.signature.params[parameterIndex].type
        return ConvertedVar(name, type)
    }

    override fun visitBooleanConstantDescriptor(
        booleanConstantDescriptor: KtBooleanConstantReference<ConeKotlinType, ConeDiagnostic>,
        data: MethodConversionContext
    ): Exp {
        return when (booleanConstantDescriptor) {
            ConeContractConstantValues.TRUE -> BoolLit(true)
            ConeContractConstantValues.FALSE -> BoolLit(false)
            else -> throw Exception("Unexpected boolean constant: $booleanConstantDescriptor")
        }
    }

    override fun visitReturnsEffectDeclaration(
        returnsEffect: KtReturnsEffectDeclaration<ConeKotlinType, ConeDiagnostic>,
        data: MethodConversionContext
    ): Exp {
        val retVar = data.returnVar.toLocalVar()
        return when (returnsEffect.value) {
            ConeContractConstantValues.WILDCARD -> BoolLit(true)
            // TODO: extend null comparisons to other types (also non-nullable), I think it is better waiting for null to work with more types
            ConeContractConstantValues.NULL -> EqCmp(retVar, NullableDomain.nullVal(Type.Int))
            ConeContractConstantValues.NOT_NULL -> NeCmp(retVar, NullableDomain.nullVal(Type.Int))
            ConeContractConstantValues.TRUE -> EqCmp(retVar, BoolLit(true))
            ConeContractConstantValues.FALSE -> EqCmp(retVar, BoolLit(false))
            else -> throw Exception("Unexpected constant: ${returnsEffect.value}")
        }
    }

    override fun visitValueParameterReference(
        valueParameterReference: KtValueParameterReference<ConeKotlinType, ConeDiagnostic>,
        data: MethodConversionContext
    ): Exp = valueParameterReference.convertedVar(data).toLocalVar()

    override fun visitIsNullPredicate(
        isNullPredicate: KtIsNullPredicate<ConeKotlinType, ConeDiagnostic>,
        data: MethodConversionContext
    ): Exp {
        val arg = isNullPredicate.arg.accept(this, data)
        // TODO: extend null comparisons to other types (also non-nullable), I think it is better waiting for null to work with more types
        return if (isNullPredicate.isNegated) {
            NeCmp(arg, NullableDomain.nullVal(Type.Int))
        } else {
            EqCmp(arg, NullableDomain.nullVal(Type.Int))
        }
    }

    override fun visitLogicalBinaryOperationContractExpression(
        binaryLogicExpression: KtBinaryLogicExpression<ConeKotlinType, ConeDiagnostic>,
        data: MethodConversionContext
    ): Exp {
        val left = binaryLogicExpression.left.accept(this, data)
        val right = binaryLogicExpression.right.accept(this, data)
        return when (binaryLogicExpression.kind) {
            LogicOperationKind.AND -> And(left, right)
            LogicOperationKind.OR -> Or(left, right)
        }
    }

    override fun visitLogicalNot(logicalNot: KtLogicalNot<ConeKotlinType, ConeDiagnostic>, data: MethodConversionContext): Exp {
        val arg = logicalNot.arg.accept(this, data)
        return Not(arg)
    }

    override fun visitConditionalEffectDeclaration(
        conditionalEffect: KtConditionalEffectDeclaration<ConeKotlinType, ConeDiagnostic>,
        data: MethodConversionContext
    ): Exp {
        val effect = conditionalEffect.effect.accept(this, data)
        val cond = conditionalEffect.condition.accept(this, data)
        return Implies(effect, cond)
    }
}