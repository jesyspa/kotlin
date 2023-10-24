/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.callables

import org.jetbrains.kotlin.formver.embeddings.LegacyUnspecifiedFunctionTypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.VariableEmbedding
import org.jetbrains.kotlin.formver.names.AnonymousName
import org.jetbrains.kotlin.formver.names.GetterFunctionName
import org.jetbrains.kotlin.formver.names.GetterFunctionSubjectName
import org.jetbrains.kotlin.formver.names.SpecialName
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.*
import org.jetbrains.kotlin.formver.viper.ast.Function

class FieldAccessFunction(
    className: MangledName,
    field: Field,
    unfoldingBody: Exp,
    pos: Position = Position.NoPosition,
    info: Info = Info.NoInfo,
    trafos: Trafos = Trafos.NoTrafos,
) : Function(GetterFunctionName(className, field.name), pos, info, trafos) {
    private val subject = Exp.LocalVar(GetterFunctionSubjectName, Type.Ref)
    private val subjectAccess = Exp.PredicateAccess(className, listOf(subject))
    override val retType: Type = field.type
    override val includeInDumpPolicy: IncludeInDumpPolicy = IncludeInDumpPolicy.PREDICATE_DUMP
    override val formalArgs: List<Declaration.LocalVarDecl> = listOf(Declaration.LocalVarDecl(GetterFunctionSubjectName, Type.Ref))
    override val pres: List<Exp> = listOf(subjectAccess)
    override val body: Exp = Exp.Unfolding(subjectAccess, unfoldingBody)
}

object DuplicableFunction : BuiltinFunction(SpecialName("duplicable")) {
    private val thisArg = VariableEmbedding(AnonymousName(0), LegacyUnspecifiedFunctionTypeEmbedding)

    override val formalArgs: List<Declaration.LocalVarDecl> = listOf(thisArg.toLocalVarDecl())
    override val retType: Type = Type.Bool
}

object SpecialFunctions {
    val all = listOf(DuplicableFunction)
}