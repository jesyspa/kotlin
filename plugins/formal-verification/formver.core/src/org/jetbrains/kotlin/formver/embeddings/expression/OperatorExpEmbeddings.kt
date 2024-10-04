/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.expression

import org.jetbrains.kotlin.formver.domains.RuntimeTypeDomain.Companion.intInjection
import org.jetbrains.kotlin.formver.embeddings.types.buildFunctionPretype
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.ne
import org.jetbrains.kotlin.formver.viper.ast.toExp

object OperatorExpEmbeddings {

    private val intIntToIntType
        get() = buildFunctionPretype {
            withParam { int() }
            withParam { int() }
            withReturnType { int() }
        }

    val AddIntInt = buildOperatorExpEmbedding {
        setName("plusInts")
        setSignature(intIntToIntType)
        viperImplementation { Exp.Add(args[0], args[1], pos, info, trafos) }
    }

    val SubIntInt = buildOperatorExpEmbedding {
        setName("minusInts")
        setSignature(intIntToIntType)
        viperImplementation { Exp.Sub(args[0], args[1], pos, info, trafos) }
    }

    val MulIntInt = buildOperatorExpEmbedding {
        setName("timesInts")
        setSignature(intIntToIntType)
        viperImplementation { Exp.Mul(args[0], args[1], pos, info, trafos) }
    }

    val DivIntInt = buildOperatorExpEmbedding {
        setName("divInts")
        setSignature(intIntToIntType)
        viperImplementation { Exp.Div(args[0], args[1], pos, info, trafos) }
        additionalConditions {
            precondition {
                intInjection.fromRef(args[1]) ne 0.toExp()
            }
        }
    }

    val RemIntInt = buildOperatorExpEmbedding {
        setName("remInts")
        setSignature(intIntToIntType)
        viperImplementation { Exp.Mod(args[0], args[1], pos, info, trafos) }
        additionalConditions {
            precondition {
                intInjection.fromRef(args[1]) ne 0.toExp()
            }
        }
    }

    private val intIntToBooleanType
        get() = buildFunctionPretype {
            withParam { int() }
            withParam { int() }
            withReturnType { boolean() }
        }

    val LeIntInt = buildOperatorExpEmbedding {
        setName("leInts")
        setSignature(intIntToBooleanType)
        viperImplementation { Exp.LeCmp(args[0], args[1], pos, info, trafos) }
    }

    val LtIntInt = buildOperatorExpEmbedding {
        setName("ltInts")
        setSignature(intIntToBooleanType)
        viperImplementation { Exp.LtCmp(args[0], args[1], pos, info, trafos) }
    }

    val GeIntInt = buildOperatorExpEmbedding {
        setName("geInts")
        setSignature(intIntToBooleanType)
        viperImplementation { Exp.GeCmp(args[0], args[1], pos, info, trafos) }
    }

    val GtIntInt = buildOperatorExpEmbedding {
        setName("gtInts")
        setSignature(intIntToBooleanType)
        viperImplementation { Exp.GtCmp(args[0], args[1], pos, info, trafos) }
    }

    val Not = buildOperatorExpEmbedding {
        setName("notBool")
        withSignature {
            withParam { boolean() }
            withReturnType { boolean() }
        }
        viperImplementation { Exp.Not(args[0], pos, info, trafos) }
    }

    private val booleanBooleanToBooleanType
        get() = buildFunctionPretype {
            withParam { boolean() }
            withParam { boolean() }
            withReturnType { boolean() }
        }

    val And = buildOperatorExpEmbedding {
        setName("andBools")
        setSignature(booleanBooleanToBooleanType)
        viperImplementation { Exp.And(args[0], args[1], pos, info, trafos) }
    }

    val Or = buildOperatorExpEmbedding {
        setName("orBools")
        setSignature(booleanBooleanToBooleanType)
        viperImplementation { Exp.Or(args[0], args[1], pos, info, trafos) }
    }

    val Implies = buildOperatorExpEmbedding {
        setName("impliesBools")
        setSignature(booleanBooleanToBooleanType)
        viperImplementation { Exp.Implies(args[0], args[1], pos, info, trafos) }
    }

    val SubCharChar = buildOperatorExpEmbedding {
        setName("subChars")
        withSignature {
            withParam { char() }
            withParam { char() }
            withReturnType { int() }
        }
        viperImplementation { Exp.Sub(args[0], args[1], pos, info, trafos) }
    }

    private val charIntToCharType = buildFunctionPretype {
        withParam { char() }
        withParam { int() }
        withReturnType { char() }
    }

    val AddCharInt = buildOperatorExpEmbedding {
        setName("addCharInt")
        setSignature(charIntToCharType)
        viperImplementation { Exp.Add(args[0], args[1], pos, info, trafos) }
    }

    val SubCharInt = buildOperatorExpEmbedding {
        setName("subCharInt")
        setSignature(charIntToCharType)
        viperImplementation { Exp.Sub(args[0], args[1], pos, info, trafos) }
    }

    private val charCharToBooleanType = buildFunctionPretype {
        withParam { char() }
        withParam { char() }
        withReturnType { boolean() }
    }

    val GeCharChar = buildOperatorExpEmbedding {
        setName("geChars")
        setSignature(charCharToBooleanType)
        viperImplementation { Exp.GeCmp(args[0], args[1], pos, info, trafos) }
    }

    val GtCharChar = buildOperatorExpEmbedding {
        setName("gtChars")
        setSignature(charCharToBooleanType)
        viperImplementation { Exp.GtCmp(args[0], args[1], pos, info, trafos) }
    }

    val LeCharChar = buildOperatorExpEmbedding {
        setName("leChars")
        setSignature(charCharToBooleanType)
        viperImplementation { Exp.LeCmp(args[0], args[1], pos, info, trafos) }
    }

    val LtCharChar = buildOperatorExpEmbedding {
        setName("ltChars")
        setSignature(charCharToBooleanType)
        viperImplementation { Exp.LtCmp(args[0], args[1], pos, info, trafos) }
    }

    val allTemplates
        get() = listOf(
            AddIntInt, SubIntInt, MulIntInt, DivIntInt, RemIntInt,
            LeIntInt, GeIntInt, LtIntInt, GtIntInt,
            Not, And, Or, Implies,
            AddCharInt, SubCharChar, SubCharInt,
            LeCharChar, GeCharChar, LtCharChar, GtCharChar,
        )
}
