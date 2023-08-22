/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.formver.scala.silicon.ast.*
import org.jetbrains.kotlin.formver.scala.silicon.ast.Exp.*
import org.jetbrains.kotlin.formver.scala.toScalaBigInt

fun invokeFunctionObject(name: FunctionObjectName, args: List<Type>, returns: Type): Method {
    val thisArg = LocalVar(ThisVariableName, Type.Ref)
    val acc = AccessPredicate.FieldAccessPredicate(
        thisArg.fieldAccess(SpecialFields.FunctionObjectCallCounterField),
        PermExp.FullPerm()
    )
    val calls = EqCmp(
        Add(Old(thisArg.fieldAccess(SpecialFields.FunctionObjectCallCounterField)), IntLit(1.toScalaBigInt())),
        thisArg.fieldAccess(SpecialFields.FunctionObjectCallCounterField)
    )
    return Method(
        name,
        listOf(Declaration.LocalVarDecl(ThisVariableName, Type.Ref)) + args.mapIndexed { index, type ->
            Declaration.LocalVarDecl(
                FunctionObjectArgName(index),
                type
            )
        },
        listOf(Declaration.LocalVarDecl(ReturnVariableName, returns)),
        listOf(acc),
        listOf(acc, calls),
        null
    )
}