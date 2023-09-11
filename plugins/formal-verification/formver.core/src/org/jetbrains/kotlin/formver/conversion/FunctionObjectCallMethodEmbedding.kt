/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.formver.embeddings.FunctionTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.MethodEmbedding
import org.jetbrains.kotlin.formver.embeddings.TypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.VariableEmbedding
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.Exp

// TODO: Deduplicate this with `InvokeFunctionObjectMethod`
class FunctionObjectCallMethodEmbedding(override val returnType: TypeEmbedding) : MethodEmbedding {
    override val name: MangledName = viperMethod.name

    override val receiver = VariableEmbedding(AnonymousName(0), FunctionTypeEmbedding)
    override val params: List<VariableEmbedding> = listOf()

    private val calls = Exp.EqCmp(
        Exp.Add(Exp.Old(receiver.toFieldAccess(SpecialFields.FunctionObjectCallCounterField)), Exp.IntLit(1)),
        receiver.toFieldAccess(SpecialFields.FunctionObjectCallCounterField)
    )

    override val preconditions: List<Exp> = receiver.accessInvariants()
    override val postconditions: List<Exp> = receiver.accessInvariants() + listOf(calls)
    override val viperMethod = InvokeFunctionObjectMethod

    override fun insertCall(argsFir: List<FirExpression>, ctx: StmtConversionContext<ResultTrackingContext>): Exp {
        val args = argsFir.map { ctx.convert(it) }
        return ctx.withResult(returnType) {
            // NOTE: Since it is only relevant to update the number of times that a function object is called,
            // the function call invocation is intentionally not assigned to the return variable
            ctx.addStatement(viperMethod.toMethodCall(args.take(1), listOf()))
            // TODO: inhale proven invariants of result.
        }
    }
}