/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.formver.embeddings.TypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.VariableEmbedding
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.Declaration
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.Exp.*
import org.jetbrains.kotlin.formver.viper.ast.Stmt
import org.jetbrains.kotlin.formver.viper.ast.Stmt.*

class InlineStmtConversionContext(
    private val methodCtx: MethodConversionContext,
    private val substitutionParams: Map<MangledName, Exp>, // parameters of the inline function to exp passed as argument
    private val substitutionInvoke: Map<MangledName, List<Stmt>>, // parameters of the inline function to lambda passed as argument
    private val inlineRetVarName: MangledName // name of the return variable in the new inline block
) :
    StmtConverter(methodCtx) {
    private var nextAnonVarNumber = 0
    private fun freshName(old: MangledName) = InlineName(methodCtx.signature.name.mangled, old.mangled)

    private fun substitute(decl: Declaration): Declaration {
        when (decl) {
            is Declaration.LocalVarDecl -> {
                val name =
                    if (substitutionParams.containsKey(decl.name)) {
                        (substitutionParams[decl.name] as LocalVar).name
                    } else {
                        freshName(decl.name)
                    }
                return decl.copy(name)
            }
        }
    }

    private fun substitute(exp: Exp): Exp {
        return when (exp) {
            is LocalVar -> when {
                exp.name == ReturnVariableName -> exp.copy(inlineRetVarName)
                substitutionParams[exp.name] != null -> substitutionParams[exp.name]!!
                else -> exp.copy(freshName(exp.name))
            }
            else -> TODO("implement substitution for $exp in an inline context")
        }
    }

    private fun substitute(stmt: Stmt): Stmt {
        return when (stmt) {
            is LocalVarAssign -> stmt.copy(substitute(stmt.lhs) as LocalVar, substitute(stmt.rhs))
            is MethodCall -> {
                val func = (stmt.args.firstOrNull() as? LocalVar)?.name
                if (stmt.methodName == InvokeFunctionObjectName.mangled && substitutionInvoke[func] != null) {
                    substitutionInvoke[func]?.forEach { super.addStatement(it) }
                    Seqn(listOf(), listOf())
                } else stmt.copy(args = stmt.args.map { substitute(it) })
            }
            else -> TODO("implement substitution for $stmt in an inline context")
        }
    }

    override fun addStatement(stmt: Stmt) {
        super.addStatement(substitute(stmt))
    }

    override fun addDeclaration(declaration: Declaration) {
        super.addDeclaration(substitute(declaration))
    }

    override fun newAnonVar(type: TypeEmbedding): VariableEmbedding =
        VariableEmbedding(AnonymousName(++nextAnonVarNumber), type)
}