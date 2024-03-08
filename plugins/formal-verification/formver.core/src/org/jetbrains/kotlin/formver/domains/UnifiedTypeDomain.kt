/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.domains

import org.jetbrains.kotlin.formver.embeddings.ClassTypeEmbedding
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.ast.*


const val UNIFIED_TYPE_DOMAIN_NAME = "UnifiedType"


class UnifiedTypeDomain(classes: List<ClassTypeEmbedding>) : BuiltinDomain(TYPE_DOMAIN_NAME) {
    override val typeVars: List<Type.TypeVar> = emptyList()

    // Define types that are not dependent on the user defined classes in a companion object.
    // That way other classes can refer to them without having an explicit reference to the concrete TypeDomain.
    companion object {
        val UnifiedType = Type.Domain(DomainName(UNIFIED_TYPE_DOMAIN_NAME).mangled, emptyList())
        val Ref = Type.Ref

        private fun createDomainFunc(funcName: String, args: List<Declaration.LocalVarDecl>, type: Type, unique: Boolean = false) =
            DomainFunc(DomainFuncName(DomainName(TYPE_DOMAIN_NAME), funcName), args, emptyList(), type, unique)

        // variables for readability improving
        private val t = Var("t", UnifiedType)
        private val t1 = Var("t1", UnifiedType)
        private val t2 = Var("t2", UnifiedType)
        private val t3 = Var("t3", UnifiedType)
        private val r = Var("t", Ref)

        // three basic functions
        /** `isSubtype: (Type, Type) -> Bool` */
        val isSubtype = createDomainFunc("isSubtype", listOf(t1.decl(), t2.decl()), Type.Bool)

        /** `typeOf: Ref -> Type` */
        val typeOf = createDomainFunc("typeOf", listOf(r.decl()), UnifiedType)

        /** `nullable: Type -> Type` */
        val nullable = createDomainFunc("nullable", listOf(t.decl()), UnifiedType)

        // many axioms will use `is` which can be represented as composition of `isSubtype` and `typeOf`
        /** `is: (Ref, Type) -> Bool` */
        fun isOf(elem: Exp, elemType: Exp) = isSubtype(typeOf(elem), elemType)

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

        data class Injection(val injectionName: String, val viperType: Type, val typeFunction: DomainFunc) {
            private val v = Var("v", viperType)
            val toRef = createDomainFunc("${injectionName}ToRef", listOf(v.decl()), Ref)
            val fromRef = createDomainFunc("${injectionName}FromRef", listOf(r.decl()), viperType)

            internal fun AxiomListBuilder.injectionAxioms() {
                axiom {
                    Exp.forall(v) { v -> simpleTrigger { isOf(toRef(v), typeFunction()) } }
                }
                axiom {
                    Exp.forall(v) { v ->
                        val expr = simpleTrigger { fromRef(toRef(v)) }
                        Exp.EqCmp(expr, v)
                    }
                }
                axiom {
                    Exp.forall(r) { r ->
                        val expr = simpleTrigger { toRef(fromRef(r)) }
                        assumption {
                            isOf(r, typeFunction())
                        }
                        Exp.EqCmp(expr, r)
                    }
                }
            }
        }

        val intInjection = Injection("int", Type.Int, intType)
        val boolInjection = Injection("bool", Type.Bool, boolType)
        val allInjections = listOf(intInjection, boolInjection)

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
            Exp.forall(t) { t -> isSubtype(t, t) }
        }
        axiom("subtype_transitive") {
            Exp.forall(t1, t2, t3) { t1, t2, t3 ->
                assumption {
                    compoundTrigger {
                        subTrigger { isSubtype(t1, t2) }
                        subTrigger { isSubtype(t2, t3) }
                    }
                }
                isSubtype(t1, t3)
            }
        }
        axiom("subtype_antisymmetric") {
            Exp.forall(t1, t2) { t1, t2 ->
                assumption {
                    compoundTrigger {
                        subTrigger { isSubtype(t1, t2) }
                        subTrigger { isSubtype(t2, t1) }
                    }
                }
                Exp.EqCmp(t1, t2)
            }
        }
        axiom("nullable_idempotent") {
            Exp.forall(t) { t ->
                val twiceNullable = nullable(nullable(t))
                simpleTrigger { twiceNullable }
                Exp.EqCmp(twiceNullable, nullable(t))
            }
        }
        axiom("nullable_supertype") {
            Exp.forall(t) { t ->
                val expr = simpleTrigger { nullable(t) }
                isSubtype(t, expr)
            }
        }
        axiom("nullable_preserves_subtype") {
            Exp.forall(t1, t2) { t1, t2 ->
                val expr = simpleTrigger { isSubtype(nullable(t1), nullable(t2)) }
                assumption { isSubtype(t1, t2) }
                expr
            }
        }
        axiom("nullable_any_supertype") {
            Exp.forall(t) { t ->
                isSubtype(t, nullable(anyType()))
            }
        }
        nonNullableTypes.forEach {
            axiom { isSubtype(it(), anyType()) }
        }
        axiom("supertype_of_nullable_nothing") {
            Exp.forall(t) { t ->
                isSubtype(nullable(nothingType()), t)
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
                    simpleTrigger { isOf(r, nullable(t)) }
                }
                Exp.EqCmp(r, nullValue()) or isOf(r, t)
            }
        }
        axiom("nothing_empty") {
            Exp.forall(r) { r ->
                !isOf(r, nothingType())
            }
        }
        axiom("null_smartcast_type_level") {
            Exp.forall(t1, t2) { t1, t2 ->
                assumption {
                    compoundTrigger {
                        isSubtype(t1, anyType())
                        isSubtype(t1, nullable(t2))
                    }
                }
                isSubtype(t1, t2)
            }
        }
        axiom("type_of_null") {
            isOf(nullValue(), nullable(nothingType()))
        }
        axiom("type_of_unit") {
            isOf(unitValue(), unitType())
        }
        axiom("uniqueness_of_unit") {
            Exp.forall(r) { r ->
                val expr = simpleTrigger { isOf(r, unitType()) }
                Exp.EqCmp(expr, unitValue())
            }
        }
        allInjections.forEach {
            it.apply { injectionAxioms() }
        }
        classTypes.forEach { (typeEmbedding, typeFunction) ->
            typeEmbedding.superTypes.forEach {
                classTypes[it]?.let { supertypeFunction ->
                    isSubtype(typeFunction(), supertypeFunction())
                }
            }
        }
    }
}
