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
                Add(args[0], args[1])
            }
            addFunction("kotlin", className = "Int", name = "minus") { args, _ ->
                Sub(args[0], args[1])
            }
            addFunction("kotlin", className = "Int", name = "times") { args, _ ->
                Mul(args[0], args[1])
            }
            addFunction("kotlin", className = "Int", name = "div") { args, _ ->
                blockOf(
                    InhaleDirect(NeCmp(args[1], IntLit(0))),
                    Div(args[0], args[1]),
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