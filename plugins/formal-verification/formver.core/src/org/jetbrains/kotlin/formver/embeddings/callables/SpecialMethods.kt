/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.callables

import org.jetbrains.kotlin.formver.embeddings.LegacyUnspecifiedFunctionTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.AnonymousVariableEmbedding
import org.jetbrains.kotlin.formver.linearization.pureToViper
import org.jetbrains.kotlin.formver.names.SpecialName
import org.jetbrains.kotlin.formver.viper.ast.BuiltInMethod
import org.jetbrains.kotlin.formver.viper.ast.Declaration
import org.jetbrains.kotlin.formver.viper.ast.Exp

object InvokeFunctionObjectMethod : BuiltInMethod(SpecialName("invoke_function_object")) {
    private val thisArg = AnonymousVariableEmbedding(0, LegacyUnspecifiedFunctionTypeEmbedding)

    override val formalArgs: List<Declaration.LocalVarDecl> = listOf(thisArg.toLocalVarDecl())
    override val formalReturns: List<Declaration.LocalVarDecl> = listOf()
    override val pres: List<Exp> = thisArg.accessInvariants().pureToViper(toBuiltin = true)
    override val posts: List<Exp> = thisArg.allAccessInvariants().pureToViper(toBuiltin = true)
}

object SpecialMethods {
    val all = listOf(InvokeFunctionObjectMethod)
}