/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.domains

import org.jetbrains.kotlin.formver.domains.RuntimeTypeDomain.Companion.isOf
import org.jetbrains.kotlin.formver.viper.ast.*
import org.jetbrains.kotlin.formver.viper.ast.Function

/**
 * Produces set of axioms for injections from built-in types in Viper to Ref.
 *
 * @param viperType: built-in type which needs to be mapped
 * @param typeFunction: representation of that type as a domain func
 */
class Injection(
    injectionName: String,
    val viperType: Type,
    val typeFunction: DomainFunc
) {
    private val v = Var("v", viperType)
    private val r = Var("r", Type.Ref)
    val toRef = RuntimeTypeDomain.createDomainFunc("${injectionName}ToRef", listOf(v.decl()), Type.Ref)
    val fromRef = RuntimeTypeDomain.createDomainFunc("${injectionName}FromRef", listOf(r.decl()), viperType)

    internal fun AxiomListBuilder.injectionAxioms() {
        axiom {
            Exp.forall(v) { v -> simpleTrigger { toRef(v) isOf typeFunction() } }
        }
        axiom {
            Exp.forall(v) { v ->
                simpleTrigger { fromRef(toRef(v)) } eq v
            }
        }
        axiom {
            Exp.forall(r) { r ->
                assumption { r isOf typeFunction() }
                simpleTrigger { toRef(fromRef(r)) } eq r
            }
        }
    }
}


/**
 * Viper function that operates on the images of an injection.
 *
 * For example, if `original` Viper function operates on `Int` and returns `Bool`,
 * then resulting `InjectionImageFunction` will take `Ref` of type `intType()` as an argument
 * and return `Ref` of type `boolType()`.
 *
 * @param argsInjections injections that must be applied to the arguments of the operation
 * @param resultInjection injection that must be applied to the result of the operation
 * @param checkDivisor adds precondition that divisor (second argument) is not zero
 */
class InjectionImageFunction(
    name: String,
    val original: Function,
    argsInjections: List<Injection>,
    resultInjection: Injection,
    checkDivisor: Boolean = false
) : Function by FunctionBuilder.build(name, {
    val viperResult = original.toFuncApp(argsInjections.map { it.fromRef(argument(Type.Ref)) })
    if (checkDivisor) {
        check(argsInjections.size == 2 && argsInjections[1] == RuntimeTypeDomain.intInjection) {
            "checkDivisor is only allowed for integer operations with two arguments"
        }
        precondition { RuntimeTypeDomain.intInjection.fromRef(args[1]) ne 0.toExp() }
    }
    postcondition { returns(Type.Ref) isOf resultInjection.typeFunction() }
    postcondition { resultInjection.fromRef(result) eq viperResult }
})