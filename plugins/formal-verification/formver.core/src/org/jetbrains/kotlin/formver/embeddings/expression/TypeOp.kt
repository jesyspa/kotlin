/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.expression

import org.jetbrains.kotlin.formver.asPosition
import org.jetbrains.kotlin.formver.domains.RuntimeTypeDomain
import org.jetbrains.kotlin.formver.embeddings.*
import org.jetbrains.kotlin.formver.embeddings.expression.debug.TreeView
import org.jetbrains.kotlin.formver.embeddings.expression.debug.debugTreeView
import org.jetbrains.kotlin.formver.embeddings.expression.debug.withDesignation
import org.jetbrains.kotlin.formver.linearization.LinearizationContext
import org.jetbrains.kotlin.formver.linearization.pureToViper
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.Stmt

data class Is(override val inner: ExpEmbedding, val comparisonType: TypeEmbedding, override val sourceRole: SourceRole? = null) :
    UnaryDirectResultExpEmbedding {
    override val type = BooleanTypeEmbedding

    override fun toViper(ctx: LinearizationContext) =
        RuntimeTypeDomain.boolInjection.toRef(
            RuntimeTypeDomain.isSubtype(
                RuntimeTypeDomain.typeOf(inner.toViper(ctx), pos = ctx.source.asPosition),
                comparisonType.runtimeType,
                pos = ctx.source.asPosition,
                info = sourceRole.asInfo
            ),
            pos = ctx.source.asPosition,
            info = sourceRole.asInfo
        )

    override val debugExtraSubtrees: List<TreeView>
        get() = listOf(comparisonType.debugTreeView.withDesignation("type"))
}


/**
 * ExpEmbedding to change the TypeEmbedding of an inner ExpEmbedding.
 * This is needed since most of our invariants require type and hence can be made more precise via Cast.
 */
data class Cast(override val inner: ExpEmbedding, override val type: TypeEmbedding) : UnaryDirectResultExpEmbedding {
    // TODO: Do we want to assert `inner isOf type` here before making a cast itself?
    override fun toViper(ctx: LinearizationContext) = inner.toViper(ctx)
    override fun ignoringCasts(): ExpEmbedding = inner.ignoringCasts()
    override fun ignoringCastsAndMetaNodes(): ExpEmbedding = inner.ignoringCastsAndMetaNodes()

    override val debugExtraSubtrees: List<TreeView>
        get() = listOf(type.debugTreeView.withDesignation("target"))
}

fun ExpEmbedding.withType(newType: TypeEmbedding): ExpEmbedding = if (type == newType) this else Cast(this, newType)


/**
 * Implementation of "safe as".
 *
 * We do some special-purpose logic here depending on whether the receiver is nullable or not, hence we cannot use `InhaleProven` directly.
 * This is also why we insist the result is stored; this is a little stronger than necessary, but that does not harm correctness.
 */
data class SafeCast(val exp: ExpEmbedding, val targetType: TypeEmbedding) : StoredResultExpEmbedding, DefaultDebugTreeViewImplementation {
    override val type: NullableTypeEmbedding
        get() = targetType.getNullable()

    override fun toViperStoringIn(result: VariableEmbedding, ctx: LinearizationContext) {
        val expViper = exp.toViper(ctx)
        val expWrapped = ExpWrapper(expViper, exp.type)
        val conditional = If(Is(expWrapped, targetType), expWrapped, NullLit, type)
        conditional.toViperStoringIn(result, ctx)
    }

    override val debugAnonymousSubexpressions: List<ExpEmbedding>
        get() = listOf(exp)

    override val debugExtraSubtrees: List<TreeView>
        get() = listOf(targetType.debugTreeView.withDesignation("type"))
}

interface InhaleInvariants: ExpEmbedding, DefaultDebugTreeViewImplementation {
    val invariants: List<TypeInvariantEmbedding>
    val exp: ExpEmbedding

    override val type: TypeEmbedding
        get() = exp.type

    override val debugAnonymousSubexpressions: List<ExpEmbedding>
        get() = listOf(exp)

    override val debugExtraSubtrees: List<TreeView>
        get() = listOf(type.debugTreeView.withDesignation("type"))

    val simplified: ExpEmbedding
        get() = if (invariants.isEmpty()) exp
        else this
}

/**
 * Augment this expression with all invariants of a certain kind that we know about the type.
 *
 * This may require storing the result in a variable, if it is not already a variable. The `simplified` property allows
 * unwrapping this type when this can be avoided.
 */
abstract class InhaleInvariantsForExp(final override val exp: ExpEmbedding) : StoredResultExpEmbedding,
    InhaleInvariants {

    override fun toViperStoringIn(result: VariableEmbedding, ctx: LinearizationContext) {
        exp.toViperStoringIn(result, ctx)
        for (invariant in invariants.fillHoles(result)) {
            ctx.addStatement { Stmt.Inhale(invariant.pureToViper(toBuiltin = true, ctx.source), ctx.source.asPosition) }
        }
    }
}

abstract class InhaleInvariantsForVariable(override val exp: ExpEmbedding, val underlyingVariable: VariableEmbedding) :
    InhaleInvariants, OnlyToViperExpEmbedding {
    override fun toViper(ctx: LinearizationContext): Exp {
        for (invariant in invariants.fillHoles(underlyingVariable)) {
            ctx.addStatement { Stmt.Inhale(invariant.pureToViper(toBuiltin = true, ctx.source), ctx.source.asPosition) }
        }
        return exp.toViper(ctx)
    }
}

private fun ExpEmbedding.withInvariants(invariants: List<TypeInvariantEmbedding>): InhaleInvariants =
    when (val variable = ignoringCastsAndMetaNodes() as? VariableEmbedding) {
        is VariableEmbedding -> object : InhaleInvariantsForVariable(this, variable) {
            override val invariants = invariants
        }
        null -> object : InhaleInvariantsForExp(this) {
            override val invariants = invariants
        }
    }

fun ExpEmbedding.withInvariants(proven: Boolean = true, access: Boolean = true): InhaleInvariants {
    val invariants = buildList {
        if (access) {
            addAll(type.accessInvariants())
        }
        if (proven) {
            add(type.subTypeInvariant())
        }
    }
    return withInvariants(invariants)
}

fun ExpEmbedding.withNewTypeInvariants(newType: TypeEmbedding, proven: Boolean = true, access: Boolean = true) =
    if (this.type == newType) this else withType(newType).withInvariants(proven, access)

