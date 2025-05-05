package org.jetbrains.kotlin.formver.purity

import org.jetbrains.kotlin.formver.embeddings.callables.UserFunctionEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.*
import org.jetbrains.kotlin.formver.embeddings.expression.debug.print
import org.jetbrains.kotlin.formver.viper.MangledName

class PurityChecker {
    // Restricted to single expressions only

    fun checkIsPure(
        userFunc: UserFunctionEmbedding //add reference to programconverter or smth to save wether this function is pure
    ): Boolean {
        val bodyExp = userFunc.body ?: return false
        val raw = bodyExp.rawExpEmbedding ?: return false
        raw.debugTreeView.print()
        return isExpressionPure(raw)
    }

    fun isExpressionPure(
        exp: ExpEmbedding
    ): Boolean = when (exp) {
        is PureExpEmbedding -> true // should cover Literals
        is Block -> {
            var isBlockPure = true
            for(exp in exp.exps){
                isBlockPure = isBlockPure && isExpressionPure(exp)
            }
            isBlockPure
        }
        is Assign -> isExpressionPure(exp.lhs)
        is PassthroughExpEmbedding -> isExpressionPure(exp.inner)
        is Goto -> true
        else -> false
    }
}