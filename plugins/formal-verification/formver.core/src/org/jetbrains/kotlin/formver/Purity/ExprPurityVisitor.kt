/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.Purity

import org.jetbrains.kotlin.formver.embeddings.expression.Assert
import org.jetbrains.kotlin.formver.embeddings.expression.Assign
import org.jetbrains.kotlin.formver.embeddings.expression.BinaryOperatorExpEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.Block
import org.jetbrains.kotlin.formver.embeddings.expression.Cast
import org.jetbrains.kotlin.formver.embeddings.expression.Declare
import org.jetbrains.kotlin.formver.embeddings.expression.Elvis
import org.jetbrains.kotlin.formver.embeddings.expression.EqCmp
import org.jetbrains.kotlin.formver.embeddings.expression.ErrorExp
import org.jetbrains.kotlin.formver.embeddings.expression.ExpEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.FieldAccess
import org.jetbrains.kotlin.formver.embeddings.expression.FieldAccessPermissions
import org.jetbrains.kotlin.formver.embeddings.expression.FieldModification
import org.jetbrains.kotlin.formver.embeddings.expression.ForAllEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.FunctionExp
import org.jetbrains.kotlin.formver.embeddings.expression.Goto
import org.jetbrains.kotlin.formver.embeddings.expression.GotoChainNode
import org.jetbrains.kotlin.formver.embeddings.expression.If
import org.jetbrains.kotlin.formver.embeddings.expression.InhaleDirect
import org.jetbrains.kotlin.formver.embeddings.expression.InhaleInvariants
import org.jetbrains.kotlin.formver.embeddings.expression.InjectionBasedExpEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.InvokeFunctionObject
import org.jetbrains.kotlin.formver.embeddings.expression.Is
import org.jetbrains.kotlin.formver.embeddings.expression.LabelExp
import org.jetbrains.kotlin.formver.embeddings.expression.LambdaExp
import org.jetbrains.kotlin.formver.embeddings.expression.MethodCall
import org.jetbrains.kotlin.formver.embeddings.expression.NeCmp
import org.jetbrains.kotlin.formver.embeddings.expression.NonDeterministically
import org.jetbrains.kotlin.formver.embeddings.expression.Old
import org.jetbrains.kotlin.formver.embeddings.expression.PredicateAccessPermissions
import org.jetbrains.kotlin.formver.embeddings.expression.PrimitiveFieldAccess
import org.jetbrains.kotlin.formver.embeddings.expression.PureExpEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.SafeCast
import org.jetbrains.kotlin.formver.embeddings.expression.SequentialAnd
import org.jetbrains.kotlin.formver.embeddings.expression.SequentialOr
import org.jetbrains.kotlin.formver.embeddings.expression.Shared
import org.jetbrains.kotlin.formver.embeddings.expression.SharingContext
import org.jetbrains.kotlin.formver.embeddings.expression.UnaryOperatorExpEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.UnitLit
import org.jetbrains.kotlin.formver.embeddings.expression.While
import org.jetbrains.kotlin.formver.embeddings.expression.WithPosition

internal object ExprPurityVisitor : ExpVisitor<Boolean> {

    /* ————— pure nodes ————— */
    override fun visitPureExpEmbedding(e: PureExpEmbedding) = true
    override fun visitUnitLit(e: UnitLit) = true

    /* ————— structural nodes without side effects ————— */
    override fun visitBlock(e: Block) = e.recurse(this)
    override fun visitIf(e: If) = e.recurse(this)
    override fun visitElvis(e: Elvis) = e.recurse(this)
    override fun visitBinaryOperatorExpEmbedding(e: BinaryOperatorExpEmbedding) =
        e.recurse(this) // TODO: the injection function should be pure per definition?

    override fun visitSequentialAnd(e: SequentialAnd) = e.recurse(this)
    override fun visitSequentialOr(e: SequentialOr) = e.recurse(this)
    override fun visitSafeCast(e: SafeCast) = e.recurse(this)
    override fun visitWithPosition(e: WithPosition) = e.recurse(this)
    override fun visitEqCmp(e: EqCmp) = e.recurse(this)
    override fun visitNeCmp(e: NeCmp) = e.recurse(this)
    override fun visitCast(e: Cast) = e.recurse(this)
    override fun visitIs(e: Is) = e.recurse(this)
    override fun visitUnaryOperatorExpEmbedding(e: UnaryOperatorExpEmbedding) = e.recurse(this)
    override fun visitForAllEmbedding(e: ForAllEmbedding) = e.recurse(this)
    override fun visitOld(e: Old) = e.recurse(this)
    override fun visitInjectionBasedExpEmbedding(e: InjectionBasedExpEmbedding) = e.recurse(this)
    override fun visitSharingContext(e: SharingContext) = e.recurse(this)

    // TODO: maybe throw errors for nodes that definitely should never appear in an Assert?
    /* ————— impure nodes ————— */
    override fun visitMethodCall(e: MethodCall) = false   // TODO: Whitelist for annotated methods?
    override fun visitFunctionExp(e: FunctionExp) = false
    override fun visitLambdaExp(e: LambdaExp) = false
    override fun visitInvokeFunctionObject(e: InvokeFunctionObject) = false
    override fun visitShared(e: Shared) = false
    override fun visitDeclare(e: Declare) = false
    override fun visitInhaleDirect(e: InhaleDirect): Boolean = false
    override fun visitErrorExp(e: ErrorExp) = false

    override fun visitAssert(e: Assert): Boolean = false
    override fun visitAssign(e: Assign): Boolean = false
    override fun visitFieldModification(e: FieldModification): Boolean = false
    override fun visitFieldAccess(e: FieldAccess): Boolean = false
    override fun visitPrimitiveFieldAccess(e: PrimitiveFieldAccess): Boolean = false
    override fun visitGoto(e: Goto): Boolean = false
    override fun visitGotoChainNode(e: GotoChainNode): Boolean = false
    override fun visitWhile(e: While): Boolean = false
    override fun visitNonDeterministically(e: NonDeterministically): Boolean = false
    override fun visitInhaleInvariants(e: InhaleInvariants): Boolean = false
    override fun visitFieldAccessPermissions(e: FieldAccessPermissions): Boolean = false
    override fun visitPredicateAccessPermissions(e: PredicateAccessPermissions): Boolean = false
    override fun visitLabelExp(e: LabelExp): Boolean = false

    override fun visitDefault(e: ExpEmbedding): Boolean = false
}

private fun ExpEmbedding.recurse(v: ExprPurityVisitor): Boolean =
    children().all { it.accept(v) }