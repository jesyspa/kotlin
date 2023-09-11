/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.formver.domains.UnitDomain
import org.jetbrains.kotlin.formver.embeddings.*
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.Stmt
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

abstract class SpecialKotlinMethodEmbedding : MethodEmbedding {
    override val preconditions: List<Exp> = listOf()
    override val postconditions: List<Exp> = listOf()
    override val viperMethod = null

    abstract val packageSegments: List<String>
    abstract val className: String?
    abstract val methodName: String
    override val name: MangledName
        get() = CallableId(FqName.fromSegments(packageSegments), className?.let { FqName(it) }, Name.identifier(methodName)).embedName()
}

object KotlinContractMethodEmbedding : SpecialKotlinMethodEmbedding() {
    override val packageSegments: List<String> = listOf("kotlin", "contracts")
    override val className: String? = null
    override val methodName: String = "contract"

    override fun insertCall(argsFir: List<FirExpression>, ctx: StmtConversionContext<ResultTrackingContext>): Exp = UnitDomain.element

    override val receiverType: TypeEmbedding? = null
    override val paramTypes: List<TypeEmbedding> = listOf(FunctionTypeEmbedding)
    override val returnType: TypeEmbedding = UnitTypeEmbedding
}

abstract class ExpressionShapedMethodEmbedding : SpecialKotlinMethodEmbedding() {
    abstract fun callExpression(args: List<Exp>): Exp
    open val callExpressionType: TypeEmbedding
        get() = returnType

    override fun insertCall(argsFir: List<FirExpression>, ctx: StmtConversionContext<ResultTrackingContext>): Exp =
        callWithConversions(argsFir, ctx) { Pair(callExpression(it), callExpressionType) }
}

abstract class BaseBinaryIntMethodEmbedding : ExpressionShapedMethodEmbedding() {
    override val packageSegments: List<String> = listOf("kotlin")
    override val className: String = "Int"

    override val receiverType: TypeEmbedding = IntTypeEmbedding
    override val paramTypes: List<TypeEmbedding> = listOf(IntTypeEmbedding)
    override val returnType: TypeEmbedding = IntTypeEmbedding
}

object KotlinIntPlusMethodEmbedding : BaseBinaryIntMethodEmbedding() {
    override val methodName: String = "plus"
    override fun callExpression(args: List<Exp>): Exp = Exp.Add(args[0], args[1])
}

object KotlinIntMinusMethodEmbedding : BaseBinaryIntMethodEmbedding() {
    override val methodName: String = "minus"
    override fun callExpression(args: List<Exp>): Exp = Exp.Sub(args[0], args[1])
}

object KotlinIntTimesMethodEmbedding : BaseBinaryIntMethodEmbedding() {
    override val methodName: String = "times"
    override fun callExpression(args: List<Exp>): Exp = Exp.Mul(args[0], args[1])
}

object KotlinIntDivMethodEmbedding : SpecialKotlinMethodEmbedding() {
    override val packageSegments: List<String> = listOf("kotlin")
    override val className: String = "Int"
    override val methodName: String = "div"

    override val receiverType: TypeEmbedding = IntTypeEmbedding
    override val paramTypes: List<TypeEmbedding> = listOf(IntTypeEmbedding)
    override val returnType: TypeEmbedding = IntTypeEmbedding

    override fun insertCall(argsFir: List<FirExpression>, ctx: StmtConversionContext<ResultTrackingContext>): Exp {
        // TODO: Get the types right here.  It's fine to wait until we get typed expression embeddings, though.
        val numerator = ctx.convert(argsFir[0])
        val denominator = ctx.convertAndStore(argsFir[1])
        ctx.addStatement(Stmt.Inhale(Exp.NeCmp(denominator, Exp.IntLit(0))))
        return Exp.Div(numerator, denominator)
    }
}

object KotlinBooleanNotMethodEmbedding : ExpressionShapedMethodEmbedding() {
    override val packageSegments: List<String> = listOf("kotlin")
    override val className: String = "Boolean"
    override val methodName: String = "not"

    override val receiverType: TypeEmbedding = BooleanTypeEmbedding
    override val paramTypes: List<TypeEmbedding> = listOf()
    override val returnType: TypeEmbedding = BooleanTypeEmbedding

    override fun callExpression(args: List<Exp>): Exp = Exp.Not(args[0])
}

object SpecialKotlinMethodEmbeddings {
    val byName = listOf(
        KotlinContractMethodEmbedding,
        KotlinIntPlusMethodEmbedding,
        KotlinIntMinusMethodEmbedding,
        KotlinIntTimesMethodEmbedding,
        KotlinIntDivMethodEmbedding,
        KotlinBooleanNotMethodEmbedding,
    ).associateBy { it.name }
}
