/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirVariable
import org.jetbrains.kotlin.fir.symbols.impl.FirVariableSymbol

/**
 * An enum for either a variable or a unique level
 * [Path] denotes a possible variable, while [Level] is for a unique level
 * This type is used for the stack for control-flow visitor
 */
sealed class PathUnique
data class Path(val symbol: FirVariableSymbol<FirVariable>) : PathUnique()
data class Level(val level: Set<UniqueLevel>) : PathUnique()

interface UniqueCheckerContext {
    val config: PluginConfiguration
    val errorCollector: ErrorCollector
    val session: FirSession
    val uniqueStack: ArrayDeque<ArrayDeque<PathUnique>>

    /**
     * Resolve the annotation for borrow and uniqueness
     */
    fun resolveUniqueAnnotation(declaration: FirDeclaration): UniqueLevel

    /**
     * Pushes the alias or unique level of the last expression onto the top of last stack.
     *
     * @param pathUnique the unique path to be added to the stack
     */
    fun pushExprPathUnique(pathUnique: PathUnique)

    fun getTopExprPathUnique(): PathUnique?
}
