/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.domains

import org.jetbrains.kotlin.formver.embeddings.ClassTypeEmbedding
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.*


const val UNIFIED_TYPE_DOMAIN_NAME = "UnifiedType"


class UnifiedTypeDomain(classes: List<ClassTypeEmbedding>) : BuiltinDomain(UNIFIED_TYPE_DOMAIN_NAME) {
    override val typeVars: List<Type.TypeVar> = emptyList()

    // Define types that are not dependent on the user defined classes in a companion object.
    // That way other classes can refer to them without having an explicit reference to the concrete TypeDomain.
    companion object {
        val UnifiedType = Type.Domain(DomainName(UNIFIED_TYPE_DOMAIN_NAME).mangled, emptyList())
        val Ref = Type.Ref

        fun createDomainFunc(funcName: String, args: List<Declaration.LocalVarDecl>, type: Type, unique: Boolean = false) =
            DomainFunc(DomainFuncName(DomainName(UNIFIED_TYPE_DOMAIN_NAME), funcName), args, emptyList(), type, unique)

        // variables for readability improving
        private val t = Var("t", UnifiedType)
        private val t1 = Var("t1", UnifiedType)
        private val t2 = Var("t2", UnifiedType)
        private val t3 = Var("t3", UnifiedType)
        private val r = Var("r", Ref)

        // three basic functions
        /** `isSubtype: (Type, Type) -> Bool` */
        val isSubtype = createDomainFunc("isSubtype", listOf(t1.decl(), t2.decl()), Type.Bool)
        infix fun Exp.subtype(otherType: Exp) = isSubtype(this, otherType)

        /** `typeOf: Ref -> Type` */
        val typeOf = createDomainFunc("typeOf", listOf(r.decl()), UnifiedType)

        /** `nullable: Type -> Type` */
        val nullable = createDomainFunc("nullable", listOf(t.decl()), UnifiedType)

        // many axioms will use `is` which can be represented as composition of `isSubtype` and `typeOf`
        /** `is: (Ref, Type) -> Bool` */
        infix fun Exp.isOf(elemType: Exp) = isSubtype(typeOf(this), elemType)

        // built-in types function
        val intType = createDomainFunc("intType", emptyList(), UnifiedType, true)
        val boolType = createDomainFunc("boolType", emptyList(), UnifiedType, true)
        val unitType = createDomainFunc("unitType", emptyList(), UnifiedType, true)
        val nothingType = createDomainFunc("nothingType", emptyList(), UnifiedType, true)
        val anyType = createDomainFunc("anyType", emptyList(), UnifiedType, true)
        val functionType = createDomainFunc("functionType", emptyList(), UnifiedType, true)

        // for creation of user types
        private fun classTypeFunc(name: MangledName) = createDomainFunc(name.mangled, emptyList(), UnifiedType, true)

        // bijections to primitive types
        val intInjection = Injection("int", Type.Int, intType)
        val boolInjection = Injection("bool", Type.Bool, boolType)
        val allInjections = listOf(intInjection, boolInjection)

        private val operationImages: MutableList<BuiltinFunction> = mutableListOf()
        fun accompanyingFunctions(): List<BuiltinFunction> = operationImages

        /**
         * Creates a Viper function that operates on the images of an injection and registers it.
         *
         * @param argsInjection injection that must be applied to the arguments of the binary operation
         * (note that it must be the same for each of arguments)
         * @param resultInjection injection that must be applied to the result of binary the operation
         * (not necessarily the same as `argsInjection`)
         * @param checkDivisor adds precondition that divisor (second argument) is not zero
         */
        private fun createBinaryOperationInjectionImage(
            name: String,
            argsInjection: Injection,
            resultInjection: Injection,
            checkDivisor: Boolean = false,
            operation: (Exp, Exp) -> Exp,
        ) = FunctionBuilder.build(name) {
            precondition { argument(Type.Ref) isOf argsInjection.typeFunction() }
            precondition { argument(Type.Ref) isOf argsInjection.typeFunction() }
            if (checkDivisor) {
                check(argsInjection == intInjection) { "checkDivisor is only allowed for integers" }
                precondition { intInjection.fromRef(arg2) ne 0.toExp() }
            }
            postcondition { returns(Type.Ref) isOf resultInjection.typeFunction() }
            val viperResult = operation(argsInjection.fromRef(arg1), argsInjection.fromRef(arg2))
            postcondition { resultInjection.fromRef(result) eq viperResult }
            body { resultInjection.toRef(viperResult) }
        }.also { operationImages.add(it) }

        private fun createUnaryOperationInjectionImage(
            name: String,
            injection: Injection,
            operation: (Exp) -> Exp
        ) = FunctionBuilder.build(name) {
            precondition { argument(Type.Ref) isOf injection.typeFunction() }
            postcondition { returns(Type.Ref) isOf injection.typeFunction() }
            val viperResult = operation(injection.fromRef(arg1))
            postcondition { injection.fromRef(result) eq viperResult }
            body { injection.toRef(viperResult) }
        }.also { operationImages.add(it) }

        // Ref translations of primitive operations
        val plusInts = createBinaryOperationInjectionImage("addInts", intInjection, intInjection) { exp1, exp2 -> exp1 + exp2 }
        val minusInts = createBinaryOperationInjectionImage("minusInts", intInjection, intInjection) { exp1, exp2 -> exp1 - exp2 }
        val timesInts = createBinaryOperationInjectionImage("timesInts", intInjection, intInjection) { exp1, exp2 -> exp1 * exp2 }
        val divInts = createBinaryOperationInjectionImage("divInts", intInjection, intInjection, true) { exp1, exp2 ->
            exp1 / exp2
        }
        val remInts = createBinaryOperationInjectionImage("remInts", intInjection, intInjection, true) { exp1, exp2 ->
            exp1 % exp2
        }
        val gtInts = createBinaryOperationInjectionImage("gtInts", intInjection, boolInjection) { exp1, exp2 -> exp1 gt exp2 }
        val ltInts = createBinaryOperationInjectionImage("ltInts", intInjection, boolInjection) { exp1, exp2 -> exp1 lt exp2 }
        val geInts = createBinaryOperationInjectionImage("geInts", intInjection, boolInjection) { exp1, exp2 -> exp1 ge exp2 }
        val leInts = createBinaryOperationInjectionImage("leInts", intInjection, boolInjection) { exp1, exp2 -> exp1 le exp2 }
        val notBool = createUnaryOperationInjectionImage("notBool", boolInjection) { !it }
        val andBools = createBinaryOperationInjectionImage("andBools", boolInjection, boolInjection) { exp1, exp2 -> exp1 and exp2 }
        val orBools = createBinaryOperationInjectionImage("orBools", boolInjection, boolInjection) { exp1, exp2 -> exp1 or exp2 }
        val impliesBools = createBinaryOperationInjectionImage("impliesBools", boolInjection, boolInjection) { exp1, exp2 ->
            exp1 implies exp2
        }

        // special values
        val nullValue = createDomainFunc("nullValue", emptyList(), Ref)
        val unitValue = createDomainFunc("unitValue", emptyList(), Ref)
    }

    val classTypes = classes.associateWith { classTypeFunc(it.className) }

    val nonNullableTypes = listOf(intType, boolType, unitType, nothingType, anyType, functionType) + classTypes.values

    override val functions: List<DomainFunc> = nonNullableTypes + listOf(nullValue, unitValue, isSubtype, typeOf, nullable) +
            allInjections.flatMap { listOf(it.toRef, it.fromRef) }

    override val axioms = AxiomListBuilder.build(this) {
        axiom("subtype_reflexive") {
            Exp.forall(t) { t -> t subtype t }
        }
        axiom("subtype_transitive") {
            Exp.forall(t1, t2, t3) { t1, t2, t3 ->
                assumption {
                    compoundTrigger {
                        subTrigger { t1 subtype t2 }
                        subTrigger { t2 subtype t3 }
                    }
                }
                t1 subtype t3
            }
        }
        axiom("subtype_antisymmetric") {
            Exp.forall(t1, t2) { t1, t2 ->
                assumption {
                    compoundTrigger {
                        subTrigger { t1 subtype t2 }
                        subTrigger { t2 subtype t1 }
                    }
                }
                t1 eq t2
            }
        }
        axiom("nullable_idempotent") {
            Exp.forall(t) { t ->
                val twiceNullable = nullable(nullable(t))
                simpleTrigger { twiceNullable }
                twiceNullable eq nullable(t)
            }
        }
        axiom("nullable_supertype") {
            Exp.forall(t) { t ->
                t subtype simpleTrigger { nullable(t) }
            }
        }
        axiom("nullable_preserves_subtype") {
            Exp.forall(t1, t2) { t1, t2 ->
                assumption { t1 subtype t2 }
                simpleTrigger { nullable(t1) subtype nullable(t2) }
            }
        }
        axiom("nullable_any_supertype") {
            Exp.forall(t) { t ->
                t subtype nullable(anyType())
            }
        }
        nonNullableTypes.forEach {
            axiom { it() subtype anyType() }
        }
        axiom("supertype_of_nullable_nothing") {
            Exp.forall(t) { t ->
                nullable(nothingType()) subtype t
            }
        }
        axiom("any_not_nullable") {
            Exp.forall(t) { t ->
                !isSubtype(nullable(t), anyType())
            }
        }
        axiom("null_smartcast_value_level") {
            Exp.forall(r, t) { r, t ->
                assumption {
                    simpleTrigger { r isOf nullable(t) }
                }
                (r eq nullValue()) or (r isOf t)
            }
        }
        axiom("nothing_empty") {
            Exp.forall(r) { r ->
                !(r isOf nothingType())
            }
        }
        axiom("null_smartcast_type_level") {
            Exp.forall(t1, t2) { t1, t2 ->
                assumption {
                    compoundTrigger {
                        subTrigger { t1 subtype anyType() }
                        subTrigger { t1 subtype nullable(t2) }
                    }
                }
                t1 subtype t2
            }
        }
        axiom("type_of_null") {
            nullValue() isOf nullable(nothingType())
        }
        axiom("type_of_unit") {
            unitValue() isOf unitType()
        }
        axiom("uniqueness_of_unit") {
            Exp.forall(r) { r ->
                assumption {
                    simpleTrigger { r isOf unitType() }
                }
                r eq unitValue()
            }
        }
        allInjections.forEach {
            it.apply { injectionAxioms() }
        }
        classTypes.forEach { (typeEmbedding, typeFunction) ->
            typeEmbedding.superTypes.forEach {
                classTypes[it]?.let { supertypeFunction ->
                    typeFunction() subtype supertypeFunction()
                }
            }
        }
    }
}
