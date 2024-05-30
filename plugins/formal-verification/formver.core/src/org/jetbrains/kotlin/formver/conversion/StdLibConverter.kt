/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.conversion

import org.jetbrains.kotlin.formver.embeddings.*
import org.jetbrains.kotlin.formver.embeddings.callables.NamedFunctionSignature
import org.jetbrains.kotlin.formver.embeddings.expression.*
import org.jetbrains.kotlin.formver.names.NameMatcher

fun VariableEmbedding.sameSize(): ExpEmbedding =
    EqCmp(FieldAccess(this, ListSizeFieldEmbedding), Old(FieldAccess(this, ListSizeFieldEmbedding)))

fun VariableEmbedding.increasedSize(amount: Int): ExpEmbedding =
    EqCmp(FieldAccess(this, ListSizeFieldEmbedding), Add(Old(FieldAccess(this, ListSizeFieldEmbedding)), IntLit(amount)))

sealed interface StdLibReceiverInterface {
    fun match(function: NamedFunctionSignature): Boolean
}

sealed interface PresentInterface : StdLibReceiverInterface {
    val interfaceName: String
    override fun match(function: NamedFunctionSignature): Boolean =
        function.receiverType?.isInheritorOfCollectionTypeNamed(interfaceName) == true
}

data object CollectionInterface : PresentInterface {
    override val interfaceName = "Collection"
}

data object ListInterface : PresentInterface {
    override val interfaceName = "List"
}

data object MutableListInterface : PresentInterface {
    override val interfaceName = "MutableList"
}

data object NoInterface : StdLibReceiverInterface {
    override fun match(function: NamedFunctionSignature): Boolean =
        NameMatcher.matchClassScope(function.name) {
            ifInCollectionsPkg {
                ifNoReceiver {
                    return true
                }
            }
            return false
        }
}

sealed interface StdLibCondition {
    val stdLibInterface: StdLibReceiverInterface
    val functionName: String

    fun match(function: NamedFunctionSignature): Boolean {
        NameMatcher.matchClassScope(function.name) {
            ifFunctionName(functionName) {
                return true
            }
            return false
        }
    }
}

sealed interface StdLibPreCondition : StdLibCondition {
    companion object {
        val all = listOf(GetPreCondition, SubListPreCondition)
    }

    fun getEmbeddings(function: NamedFunctionSignature): List<ExpEmbedding>
}

sealed interface StdLibPostCondition : StdLibCondition {
    companion object {
        val all = listOf(EmptyListPostCondition, IsEmptyPostCondition, GetPostCondition, SubListPostCondition, AddPostCondition)
    }

    fun getEmbeddings(returnVariable: VariableEmbedding, function: NamedFunctionSignature): List<ExpEmbedding>
}

data object GetPreCondition : StdLibPreCondition {
    override fun getEmbeddings(function: NamedFunctionSignature): List<ExpEmbedding> {
        val receiver = function.receiver!!
        val indexArg = function.formalArgs[1]
        return listOf(
            GeCmp(
                indexArg,
                IntLit(0),
                SourceRole.ListElementAccessCheck(SourceRole.ListElementAccessCheck.AccessCheckType.LESS_THAN_ZERO)
            ),
            GtCmp(
                FieldAccess(receiver, ListSizeFieldEmbedding),
                indexArg,
                SourceRole.ListElementAccessCheck(SourceRole.ListElementAccessCheck.AccessCheckType.GREATER_THAN_LIST_SIZE)
            ),
        )
    }

    override val stdLibInterface = ListInterface
    override val functionName = "get"
}

data object SubListPreCondition : StdLibPreCondition {
    override fun getEmbeddings(function: NamedFunctionSignature): List<ExpEmbedding> {
        val receiver = function.receiver!!
        val fromIndexArg = function.formalArgs[1]
        val toIndexArg = function.formalArgs[2]
        return listOf(
            LeCmp(fromIndexArg, toIndexArg, SourceRole.SubListCreation.CheckInSize),
            GeCmp(fromIndexArg, IntLit(0), SourceRole.SubListCreation.CheckNegativeIndices),
            LeCmp(toIndexArg, FieldAccess(receiver, ListSizeFieldEmbedding), SourceRole.SubListCreation.CheckInSize)
        )
    }

    override val stdLibInterface = ListInterface
    override val functionName = "subList"
}

data object EmptyListPostCondition : StdLibPostCondition {
    override fun getEmbeddings(returnVariable: VariableEmbedding, function: NamedFunctionSignature): List<ExpEmbedding> {
        return listOf(
            EqCmp(FieldAccess(returnVariable, ListSizeFieldEmbedding), IntLit(0))
        )
    }

    override val stdLibInterface = NoInterface
    override val functionName = "emptyList"
}

data object IsEmptyPostCondition : StdLibPostCondition {
    override fun getEmbeddings(returnVariable: VariableEmbedding, function: NamedFunctionSignature): List<ExpEmbedding> {
        val receiver = function.receiver!!
        return listOf(
            receiver.sameSize(),
            Implies(returnVariable, EqCmp(FieldAccess(receiver, ListSizeFieldEmbedding), IntLit(0))),
            Implies(Not(returnVariable), GtCmp(FieldAccess(receiver, ListSizeFieldEmbedding), IntLit(0)))
        )
    }

    override val stdLibInterface = CollectionInterface
    override val functionName = "isEmpty"
}

data object GetPostCondition : StdLibPostCondition {
    override fun getEmbeddings(returnVariable: VariableEmbedding, function: NamedFunctionSignature): List<ExpEmbedding> {
        return listOf(function.receiver!!.sameSize())
    }

    override val stdLibInterface = ListInterface
    override val functionName = "get"
}

data object SubListPostCondition : StdLibPostCondition {
    override fun getEmbeddings(returnVariable: VariableEmbedding, function: NamedFunctionSignature): List<ExpEmbedding> {
        val fromIndexArg = function.formalArgs[1]
        val toIndexArg = function.formalArgs[2]
        return listOf(
            function.receiver!!.sameSize(),
            EqCmp(FieldAccess(returnVariable, ListSizeFieldEmbedding), Sub(toIndexArg, fromIndexArg))
        )
    }

    override val stdLibInterface = ListInterface
    override val functionName = "subList"
}

data object AddPostCondition : StdLibPostCondition {
    override fun getEmbeddings(returnVariable: VariableEmbedding, function: NamedFunctionSignature): List<ExpEmbedding> {
        return listOf(function.receiver!!.increasedSize(1))
    }

    override val stdLibInterface = MutableListInterface
    override val functionName = "add"
}

fun NamedFunctionSignature.stdLibPreConditions(): List<ExpEmbedding> {
    StdLibPreCondition.all.groupBy {
        it.stdLibInterface
    }.forEach { (stdLibInterface, preConditions) ->
        if (stdLibInterface.match(this)) {
            preConditions.forEach {
                if (it.match(this)) {
                    return it.getEmbeddings(this)
                }
            }
        }
    }
    return listOf()
}


fun NamedFunctionSignature.stdLibPostConditions(returnVariable: VariableEmbedding): List<ExpEmbedding> {
    StdLibPostCondition.all.groupBy {
        it.stdLibInterface
    }.forEach { (stdLibInterface, postConditions) ->
        if (stdLibInterface.match(this)) {
            postConditions.forEach {
                if (it.match(this)) {
                    return it.getEmbeddings(returnVariable, this)
                }
            }
        }
    }
    return listOf()
}
