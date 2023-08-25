/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.formver.scala.MangledName
import org.jetbrains.kotlin.formver.scala.silicon.ast.Declaration
import org.jetbrains.kotlin.formver.scala.silicon.ast.Exp
import org.jetbrains.kotlin.formver.scala.silicon.ast.Exp.*
import org.jetbrains.kotlin.formver.scala.silicon.ast.Stmt
import org.jetbrains.kotlin.formver.scala.silicon.ast.Stmt.*

fun Declaration.substitute(old: MangledName, new: MangledName): Declaration {
    return when (this) {
        is Declaration.LocalVarDecl -> if (name == old) this.copy(new) else this
    }
}

fun Stmt.substitute(old: MangledName, new: MangledName): Stmt {
    return when (this) {
        is MethodCall -> this.copy(methodName, args.map { it.substitute(old, new) }, targets)
        is LocalVarAssign -> this.copy(lhs.substitute(old, new) as LocalVar, rhs.substitute(old, new))
        else -> this
    }
}

fun Exp.substitute(old: MangledName, new: MangledName): Exp {
    return when (this) {
        is LocalVar -> if (name == old) this.copy(new) else this
        is And -> And(left.substitute(old, new), right.substitute(old, new))
        else -> this
    }
}