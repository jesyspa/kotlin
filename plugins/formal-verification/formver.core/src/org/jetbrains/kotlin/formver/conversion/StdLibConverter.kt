/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.formver.embeddings.callables.FunctionSignature
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.Exp.*
import org.jetbrains.kotlin.formver.viper.ast.PermExp

fun LocalVar.list(): List<Exp> =
    listOf(
        fieldAccessPredicate(SpecialFields.ListSizeField, PermExp.FullPerm()),
        GeCmp(fieldAccess(SpecialFields.ListSizeField), IntLit(0))
    )

fun LocalVar.sameSize(): Exp = EqCmp(fieldAccess(SpecialFields.ListSizeField), Old(fieldAccess(SpecialFields.ListSizeField)))
fun LocalVar.increasedSize(amount: Int): Exp =
    EqCmp(fieldAccess(SpecialFields.ListSizeField), Add(Old(fieldAccess(SpecialFields.ListSizeField)), IntLit(amount)))

fun FirFunctionSymbol<*>.stdLibPreConditions(signature: FunctionSignature): List<Exp> {
    // TODO: filter by List in SuperTypes
    val typeInvariants = signature.formalArgs
        .filter { it.type.name.mangled == "T_class_pkg\$kotlin\$collections\$global\$class_List" || it.type.name.mangled == "T_class_pkg\$kotlin\$collections\$global\$class_MutableList" }
        .map { it.toViper().list() }
        .flatten()
    val customInvariants =
        if (callableId.packageName.asString() == "kotlin.collections") {
            when (callableId.callableName.asString()) {
                "emptyList" -> listOf()
                "get" -> {
                    val receiver = signature.receiver!!.toViper()
                    val indexArg = signature.formalArgs[1].toViper()
                    listOf(
                        GeCmp(indexArg, IntLit(0)),
                        GtCmp(receiver.fieldAccess(SpecialFields.ListSizeField), indexArg),
                    )
                }
                else -> listOf()
            }
        } else {
            listOf()
        }
    return typeInvariants + customInvariants
}


fun FirFunctionSymbol<*>.stdLibPostConditions(signature: FunctionSignature): List<Exp> {
    // TODO: filter by List in SuperTypes
    val typeInvariants = (signature.formalArgs + signature.returnVar)
        .filter { it.type.name.mangled == "T_class_pkg\$kotlin\$collections\$global\$class_List" || it.type.name.mangled == "T_class_pkg\$kotlin\$collections\$global\$class_MutableList" }
        .map { it.toViper().list() }
        .flatten()

    val retVar = LocalVar(ReturnVariableName, signature.returnType.viperType)
    val receiver = signature.receiver?.toViper()
    val customInvariants =
        if (callableId.packageName.asString() == "kotlin.collections") {
            when (callableId.callableName.asString()) {
                "emptyList" -> listOf(
                    EqCmp(retVar.fieldAccess(SpecialFields.ListSizeField), IntLit(0))
                )
                "get" -> {
                    listOf(receiver!!.sameSize())
                }
                "add" -> {
                    listOf(receiver!!.increasedSize(1))
                }
                "isEmpty" -> {
                    listOf(
                        receiver!!.sameSize(),
                        Implies(retVar, EqCmp(receiver.fieldAccess(SpecialFields.ListSizeField), IntLit(0))),
                        Implies(Not(retVar), GtCmp(receiver.fieldAccess(SpecialFields.ListSizeField), IntLit(0)))
                    )
                }
                else -> listOf()
            }
        } else {
            listOf()
        }
    return typeInvariants + customInvariants
}