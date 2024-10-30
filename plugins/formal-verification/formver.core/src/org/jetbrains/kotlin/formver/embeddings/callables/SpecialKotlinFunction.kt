/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.callables

import org.jetbrains.kotlin.formver.embeddings.expression.*
import org.jetbrains.kotlin.formver.embeddings.types.buildFunctionPretype
import org.jetbrains.kotlin.formver.names.*
import org.jetbrains.kotlin.formver.names.NameMatcher
import org.jetbrains.kotlin.formver.viper.ast.Method
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.formver.embeddings.expression.OperatorExpEmbeddings.AddIntInt
import org.jetbrains.kotlin.formver.embeddings.expression.OperatorExpEmbeddings.AddCharInt
import org.jetbrains.kotlin.formver.embeddings.expression.OperatorExpEmbeddings.DivIntInt
import org.jetbrains.kotlin.formver.embeddings.expression.OperatorExpEmbeddings.MulIntInt
import org.jetbrains.kotlin.formver.embeddings.expression.OperatorExpEmbeddings.Not
import org.jetbrains.kotlin.formver.embeddings.expression.OperatorExpEmbeddings.SubCharChar
import org.jetbrains.kotlin.formver.embeddings.expression.OperatorExpEmbeddings.SubCharInt
import org.jetbrains.kotlin.formver.embeddings.expression.OperatorExpEmbeddings.SubIntInt

/**
 * Kotlin function that should be handled specially by our conversion.
 *
 * This includes `contract` and operations on primitive types, where providing a full embedding into Viper
 * offers more possibilities for reasoning about the code.
 */
interface SpecialKotlinFunction : FunctionEmbedding {
    val packageName: List<String>
    val className: String?
        get() = null
    val name: String
    override val viperMethod: Method?
        get() = null
}

val SpecialKotlinFunction.callableId: CallableId
    get() = CallableId(FqName.fromSegments(packageName), className?.let { FqName(it) }, Name.identifier(name))

fun SpecialKotlinFunction.embedName(): ScopedKotlinName = callableId.embedFunctionName(callableType)


object SpecialKotlinFunctions {
    private val contractBuilderTypeName = buildName {
        packageScope(listOf("kotlin", "contracts"))
        ClassKotlinName(listOf("ContractBuilder"))
    }
    private val booleanArrayTypeName = buildName {
        packageScope(listOf("kotlin"))
        ClassKotlinName(listOf("BooleanArray"))
    }

    val byName = buildSpecialFunctions {
        val intIntToIntType = buildFunctionPretype {
            withDispatchReceiver { int() }
            withParam { int() }
            withReturnType { int() }
        }
        withCallableType(intIntToIntType) {
            addFunction("kotlin", className = "Int", name = "plus") { args, _ ->
                AddIntInt(args[0], args[1])
            }
            addFunction("kotlin", className = "Int", name = "minus") { args, _ ->
                SubIntInt(args[0], args[1])
            }
            addFunction("kotlin", className = "Int", name = "times") { args, _ ->
                MulIntInt(args[0], args[1])
            }
            addFunction("kotlin", className = "Int", name = "div") { args, _ ->
                blockOf(
                    InhaleDirect(NeCmp(args[1], IntLit(0))),
                    DivIntInt(args[0], args[1]),
                )
            }
        }

        val booleanToBooleanType = buildFunctionPretype {
            withDispatchReceiver { boolean() }
            withReturnType { boolean() }
        }

        addFunction(booleanToBooleanType, "kotlin", className = "Boolean", name = "not") { args, _ ->
            Not(args[0])
        }

        val verifyCallableType = buildFunctionPretype {
            withParam {
                klass {
                    withName(booleanArrayTypeName)
                }
            }
            withReturnType { unit() }
        }
        addFunction(verifyCallableType, "org", "jetbrains", "kotlin", "formver", "plugin", name = "verify") { args, _ ->
            args.map { Assert(it) }.toBlock()
        }

        val contractCallableType = buildFunctionPretype {
            withParam {
                function {
                    withDispatchReceiver {
                        klass {
                            withName(contractBuilderTypeName)
                        }
                    }
                    withReturnType { unit() }
                }
            }
            withReturnType { unit() }
        }

        addFunction(contractCallableType, "kotlin", "contracts", name = "contract") { _, _ ->
            UnitLit
        }

        val charCharToIntType = buildFunctionPretype {
            withDispatchReceiver { char() }
            withParam { char() }
            withReturnType { int() }
        }

        addFunction(charCharToIntType, "kotlin", className = "Char", name = "minus") { args, _ ->
            SubCharChar(args[0], args[1])
        }

        val charIntToCharType = buildFunctionPretype {
            withDispatchReceiver { char() }
            withParam { int() }
            withReturnType { char() }
        }

        withCallableType(charIntToCharType) {
            addFunction("kotlin", className = "Char", name = "plus") { args, _ ->
                AddCharInt(args[0], args[1])
            }
            addFunction("kotlin", className = "Char", name = "minus") { args, _ ->
                SubCharInt(args[0], args[1])
            }
        }

        val intCharToCharType = buildFunctionPretype {
            withDispatchReceiver { int() }
            withParam { char() }
            withReturnType { char() }
        }
    }
}

val FunctionEmbedding.isVerifyFunction: Boolean
    get() = this is SpecialKotlinFunction && NameMatcher.matchClassScope(this.embedName()) {
        ifPackageName("org", "jetbrains", "kotlin", "formver", "plugin") {
            ifNoReceiver {
                ifFunctionName("verify") {
                    return true
                }
            }
        }
        return false
    }