/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.embeddings

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.formver.asSilverPosition
import org.jetbrains.kotlin.formver.conversion.ResultTrackingContext
import org.jetbrains.kotlin.formver.conversion.StmtConversionContext
import org.jetbrains.kotlin.formver.conversion.withResult
import org.jetbrains.kotlin.formver.viper.ast.Stmt

abstract class BackingFieldAccess(val field: FieldEmbedding) {
    fun <RTC : ResultTrackingContext> access(
        receiver: ExpEmbedding,
        ctx: StmtConversionContext<RTC>,
        source: KtSourceElement?,
        action: StmtConversionContext<RTC>.(access: FieldAccess) -> Unit,
    ) {
        val invariant = field.accessInvariantForAccess(receiver.toViper())
        invariant?.let {
            ctx.addStatement(Stmt.Inhale(it, source.asSilverPosition))
        }
        ctx.action(FieldAccess(receiver, field, source.asSilverPosition))
        invariant?.let {
            ctx.addStatement(Stmt.Exhale(it, source.asSilverPosition))
        }
    }
}

class BackingFieldGetter(field: FieldEmbedding) : BackingFieldAccess(field), GetterEmbedding {
    override fun getValue(
        receiver: ExpEmbedding,
        ctx: StmtConversionContext<ResultTrackingContext>,
        source: KtSourceElement?,
    ): ExpEmbedding =
        ctx.withResult(field.type) {
            access(receiver, this, source) {
                addStatement(Stmt.assign(resultExp.toViper(), it.toViper(), source.asSilverPosition))
                field.type.provenInvariants(resultExp.toViper()).forEach { inv ->
                    addStatement(Stmt.Inhale(inv, source.asSilverPosition))
                }
            }
        }
}

class BackingFieldSetter(field: FieldEmbedding) : BackingFieldAccess(field), SetterEmbedding {
    override fun setValue(
        receiver: ExpEmbedding,
        value: ExpEmbedding,
        ctx: StmtConversionContext<ResultTrackingContext>,
        source: KtSourceElement?,
    ) {
        access(receiver, ctx, source) {
            addStatement(Stmt.assign(it.toViper(), value.withType(field.type).toViper(), source.asSilverPosition))
        }
    }
}

