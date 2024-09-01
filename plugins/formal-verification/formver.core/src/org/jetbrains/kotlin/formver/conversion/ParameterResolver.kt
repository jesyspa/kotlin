/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.formver.embeddings.callables.FunctionSignature
import org.jetbrains.kotlin.formver.embeddings.expression.ExpEmbedding
import org.jetbrains.kotlin.formver.names.ExtraSpecialNames
import org.jetbrains.kotlin.formver.names.embedParameterName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

/**
 * Name resolver for parameters and return values and labels.
 *
 * Since parameter names may map to lambda embeddings, we use `embed` for those for consistency.
 */
interface ParameterResolver {
    fun tryResolveParameter(name: Name): ExpEmbedding?
    fun tryResolveDispatchReceiver(): ExpEmbedding?
    fun tryResolveExtensionReceiver(labelName: String): ExpEmbedding?

    val labelName: String?
    val defaultResolvedReturnTarget: ReturnTarget
}

fun ParameterResolver.resolveNamedReturnTarget(returnPointName: String): ReturnTarget? =
    (returnPointName == labelName).ifTrue { defaultResolvedReturnTarget }

class RootParameterResolver(
    val ctx: ProgramConversionContext,
    private val signature: FunctionSignature,
    override val labelName: String?,
    override val defaultResolvedReturnTarget: ReturnTarget,
) : ParameterResolver {
    private val parameters = signature.params.associateBy { it.name }
    override fun tryResolveParameter(name: Name): ExpEmbedding? = parameters[name.embedParameterName()]
    override fun tryResolveDispatchReceiver() = signature.dispatchReceiver
    override fun tryResolveExtensionReceiver(labelName: String) = (labelName == this.labelName).ifTrue {
        signature.extensionReceiver
    }
}

class InlineParameterResolver(
    private val substitutions: Map<Name, ExpEmbedding>,
    override val labelName: String?,
    override val defaultResolvedReturnTarget: ReturnTarget,
) : ParameterResolver {
    override fun tryResolveParameter(name: Name): ExpEmbedding? = substitutions[name]
    override fun tryResolveDispatchReceiver() = substitutions[ExtraSpecialNames.DISPATCH_THIS]
    override fun tryResolveExtensionReceiver(labelName: String) = (labelName == this.labelName).ifTrue {
        substitutions[ExtraSpecialNames.EXTENSION_THIS]
    }
}