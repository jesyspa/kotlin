/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.formver.linearization

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.formver.embeddings.TypeEmbedding
import org.jetbrains.kotlin.formver.embeddings.VariableEmbedding
import org.jetbrains.kotlin.formver.viper.ast.Declaration
import org.jetbrains.kotlin.formver.viper.ast.Exp
import org.jetbrains.kotlin.formver.viper.ast.Stmt

class PureLinearizerError(val offendingFunction: String) : IllegalStateException(offendingFunction)

class PureLinearizer(override val source: KtSourceElement) : LinearizationContext {
    override fun withPosition(newPosition: KtSourceElement, action: LinearizationContext.() -> Unit) {
        PureLinearizer(newPosition).action()
    }

    override fun newVar(type: TypeEmbedding): VariableEmbedding {
        throw PureLinearizerError("newVar")
    }

    override fun inhaleForThisStatement(assumption: Exp) {
        throw PureLinearizerError("inhaleForThisStatement")
    }

    override fun withNewScope(action: LinearizationContext.() -> Unit): Stmt.Seqn {
        throw PureLinearizerError("withNewScope")
    }

    override fun addStatement(stmt: Stmt) {
        throw PureLinearizerError("addStatement")
    }

    override fun addDeclaration(decl: Declaration) {
        throw PureLinearizerError("addDeclaration")
    }

    override val block: Stmt.Seqn
        get() = throw PureLinearizerError("block")
}
