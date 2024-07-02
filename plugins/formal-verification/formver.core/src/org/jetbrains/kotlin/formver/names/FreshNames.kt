/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.names

/* This file contains mangled names for constructs introduced during the conversion to Viper.
 *
 * See the NameEmbeddings file for guidelines on good name choices.
 */

open class SimpleName(name: String) : ResolvableImplMixin() {
    override val primary = ChunkedName(name)
}

/**
 * Representation for names not present in the original source,
 * e.g. storage for the result of subexpressions.
 */
data class AnonymousName(val n: Int) : SimpleName("anonymous\$$n")

/**
 * Name for return variable that should *only* be used in signatures of methods without a body.
 */
data object PlaceholderReturnVariableName : SimpleName("ret")

data class ReturnVariableName(val n: Int) : SimpleName("ret\$$n")
data object ThisReceiverName : SimpleName("this")
data object SetterValueName : SimpleName("value")
data object ClassPredicateSubjectName : SimpleName("subject")

abstract class NumberedLabelName(kind: String, n: Int) : SimpleName("label\$$kind\$$n")

data class ReturnLabelName(val scopeDepth: Int) : NumberedLabelName("ret", scopeDepth)
data class BreakLabelName(val n: Int) : NumberedLabelName("break", n)
data class ContinueLabelName(val n: Int) : NumberedLabelName("continue", n)
data class CatchLabelName(val n: Int) : NumberedLabelName("catch", n)
data class TryExitLabelName(val n: Int) : NumberedLabelName("try_exit", n)

data object SpecialNamespace : ResolvableNamespace, ResolvableImplMixin() {
    override val phase: ResolutionPhase = PackagePhase
    override val primary = ChunkedName("special")
}

fun specialName(name: String): Resolvable = NamespacedName(SpecialNamespace, SimpleName(name))