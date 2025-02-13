/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.formver.embeddings.callables.FunctionEmbedding
import org.jetbrains.kotlin.formver.embeddings.callables.isLoopInvariantsFunction
import org.jetbrains.kotlin.formver.embeddings.callables.isPreconditionsFunction

private const val SPECIFICATION_CONDITION_ERROR =
    "Only a single `preconditions` scope and a single `postconditions` scope is allowed as a function specification."

private const val SPECIFICATION_LOOP_ERROR =
    "Only a single `loopInvariants` scope is allowed in the loop."

sealed interface FormverFirBlock {
    val statements: FirBlock
}

data class LoopInvariantsBlock(override val statements: FirBlock) : FormverFirBlock
data class PreconditionsBlock(override val statements: FirBlock) : FormverFirBlock
data class PostconditionsBlock(override val statements: FirBlock) : FormverFirBlock

fun FunctionEmbedding.blockType(): ((FirBlock) -> FormverFirBlock)? =
    when {
        isLoopInvariantsFunction -> ::LoopInvariantsBlock
        isPreconditionsFunction -> ::PreconditionsBlock
        else -> null
    }

private fun ProgramConversionContext.extractFormverFirBlocks(parentBlock: FirBlock): List<FormverFirBlock> = buildList {
    parentBlock.statements.forEach { call ->
        if (call !is FirFunctionCall) return@buildList
        val firFunction = call.toResolvedCallableSymbol() as FirFunctionSymbol<*>
        val functionEmbedding = embedFunction(firFunction)
        val constructor = functionEmbedding.blockType() ?: return@buildList
        val formverSpecificationArgument = call.argument
        check(formverSpecificationArgument is FirAnonymousFunctionExpression) {
            "Only lambdas are allowed as arguments of ${firFunction.name}."
        }
        add(constructor(formverSpecificationArgument.anonymousFunction.body!!))
    }
}

fun ProgramConversionContext.extractLoopInvariants(parentBlock: FirBlock): FirBlock? {
    val formverBlocks = extractFormverFirBlocks(parentBlock)
    val block = formverBlocks.firstOrNull() ?: return null
    if (block !is LoopInvariantsBlock || formverBlocks.size != 1)
        error(SPECIFICATION_LOOP_ERROR)
    return block.statements
}

fun ProgramConversionContext.extractFunctionSpecification(parentBlock: FirBlock): Pair<FirBlock?, FirBlock?> {
    val formverBlocks = extractFormverFirBlocks(parentBlock)
    val firstBlock = formverBlocks.firstOrNull() ?: return null to null
    val secondBlock = formverBlocks.getOrNull(1) ?: return when (firstBlock) {
        is PreconditionsBlock -> firstBlock.statements to null
        is PostconditionsBlock -> null to firstBlock.statements
        else -> error(SPECIFICATION_CONDITION_ERROR)
    }
    if (firstBlock is PreconditionsBlock && secondBlock is PostconditionsBlock) {
        return firstBlock.statements to secondBlock.statements
    } else error(SPECIFICATION_CONDITION_ERROR)
}

