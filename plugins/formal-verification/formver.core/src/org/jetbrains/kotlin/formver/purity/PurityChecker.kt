package org.jetbrains.kotlin.formver.purity

import org.jetbrains.kotlin.formver.conversion.ProgramConverter
import org.jetbrains.kotlin.formver.embeddings.callables.UserFunctionEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.*
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.mangled

class PurityChecker {
    // Map: true = pure, false = impure
    private val registry = mutableMapOf<MangledName, Boolean>()
    private val declaredPure = mutableMapOf<MangledName, Boolean>()
    private var programConverter: ProgramConverter? = null

    fun declareFunctionAnnotation(
        userFunc: UserFunctionEmbedding,
        hasPureAnnotation: Boolean,
        programConverter: ProgramConverter
    ) {
        val key = programConverter.getMangledName(userFunc)
        declaredPure[key] = hasPureAnnotation
    }

    fun checkIsPure(
        userFunc: UserFunctionEmbedding,
        programConverter: ProgramConverter
    ): Boolean {
        this.programConverter = programConverter
        val rawBody = userFunc.body?.rawExpEmbedding ?: return false
        val key = programConverter.getMangledName(userFunc)

        // If function is annotated non-pure → immediately impure
        if (declaredPure[key] == false) {
            registry[key] = false
            return false
        }

        // Cached?
        registry[key]?.let { return it }

        // First visit – evaluate and cache
        val result = isExpressionPure(rawBody)
        registry[key] = result
        return result
    }

    // ─────────────────────────────────────────────────────────────
    private fun isExpressionPure(exp: ExpEmbedding): Boolean = when (exp) {
        is PureExpEmbedding -> true

        is Block -> exp.exps.all(::isExpressionPure)

        is Assign -> if (exp.lhs is PlaceholderVariableEmbedding)
            isExpressionPure(exp.rhs)
        else
            isExpressionPure(exp.lhs) && isExpressionPure(exp.rhs)

        is PassthroughExpEmbedding -> isExpressionPure(exp.inner)

        // Operators
        is BinaryDirectResultExpEmbedding ->
            isExpressionPure(exp.left) && isExpressionPure(exp.right)

        is UnaryDirectResultExpEmbedding ->
            isExpressionPure(exp.inner)

        // Function calls
        is MethodCall -> {
            val mangledName = exp.method.name.mangled
            val argsPure = exp.args.all(::isExpressionPure)
            val operatorPure =
                PureOperatorWhitelist.isWhitelisted(mangledName) && argsPure

            val calleePure = registry[exp.method.name] == true

            operatorPure || calleePure
        }

        // Single-expression If
        is If -> isExpressionPure(exp.condition) &&
                isExpressionPure(exp.thenBranch) &&
                isExpressionPure(exp.elseBranch)

        is Goto -> true          // auto-generated return
        is InhaleDirect -> true  // auto-generated check
        else -> false
    }

    // ─────────── helpers for statement counting (single-expression rule) ───────────
    private fun unwrap(exp: ExpEmbedding): ExpEmbedding =
        if (exp is PassthroughExpEmbedding) unwrap(exp.inner) else exp

    private fun collectTopLevel(exp: ExpEmbedding): List<ExpEmbedding> {
        val core = unwrap(exp)
        return when (core) {
            is Block -> core.exps.flatMap(::collectTopLevel)
            else -> listOf(core)
        }
    }

    private fun ignoreForCounting(exp: ExpEmbedding): Boolean =
        exp is InhaleDirect

    private fun countRealTopLevel(body: ExpEmbedding): Int =
        collectTopLevel(body)
            .filterNot(::ignoreForCounting)
            .size
}
