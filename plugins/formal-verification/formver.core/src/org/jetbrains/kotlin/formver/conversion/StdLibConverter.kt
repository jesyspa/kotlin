/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.formver.embeddings.callables.NamedFunctionSignature
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.Exp.*

fun LocalVar.sameSize(): Exp = EqCmp(fieldAccess(SpecialFields.ListSizeField), Old(fieldAccess(SpecialFields.ListSizeField)))
fun LocalVar.increasedSize(amount: Int): Exp =
    EqCmp(fieldAccess(SpecialFields.ListSizeField), Add(Old(fieldAccess(SpecialFields.ListSizeField)), IntLit(amount)))

fun FirFunctionSymbol<*>.stdLibPreConditions(signature: NamedFunctionSignature): List<Exp> =
    if (signature.name.isCollection) {
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


fun FirFunctionSymbol<*>.stdLibPostConditions(signature: NamedFunctionSignature): List<Exp> {
    val retVar = LocalVar(ReturnVariableName, signature.returnType.viperType)
    val receiver = signature.receiver?.toViper()
    val customInvariants =
        if (signature.name.isCollection) {
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
    return customInvariants
}