/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings.expression

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.formver.asPosition
import org.jetbrains.kotlin.formver.domains.NullableDomain
import org.jetbrains.kotlin.formver.domains.UnitDomain
import org.jetbrains.kotlin.formver.embeddings.*
import org.jetbrains.kotlin.formver.viper.ast.Exp

data object UnitLit : PureExpEmbedding {
    override val type = UnitTypeEmbedding
    override fun toViper(source: KtSourceElement?) = UnitDomain.element
}

data class IntLit(val value: Int) : PureExpEmbedding {
    override val type = IntTypeEmbedding

    override fun toViper(source: KtSourceElement?): Exp = Exp.IntLit(value, source.asPosition)
}

data class BooleanLit(val value: Boolean) : PureExpEmbedding {
    override val type = BooleanTypeEmbedding

    override fun toViper(source: KtSourceElement?): Exp = Exp.BoolLit(value, source.asPosition)
}

data class NullLit(val elemType: TypeEmbedding) : PureExpEmbedding {
    override val type = NullableTypeEmbedding(elemType)
    override fun toViper(source: KtSourceElement?): Exp = NullableDomain.nullVal(elemType.viperType, source)
}
