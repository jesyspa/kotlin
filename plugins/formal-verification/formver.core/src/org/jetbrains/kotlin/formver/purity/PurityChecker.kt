package org.jetbrains.kotlin.formver.purity

import org.jetbrains.kotlin.formver.embeddings.callables.UserFunctionEmbedding
import org.jetbrains.kotlin.formver.embeddings.expression.*
import org.jetbrains.kotlin.formver.viper.MangledName
import org.jetbrains.kotlin.formver.viper.mangled

class PurityChecker() {

    private val registry = mutableMapOf<MangledName, PurityStatus>()
    private val declaredPure = mutableMapOf<MangledName, Boolean>()

    private val waiting = mutableMapOf<MangledName, MutableSet<UserFunctionEmbedding>>()
    private var currentEvaluatedFunction: UserFunctionEmbedding? = null

    // Debugging Messages
    private var debugSink: ((String) -> Unit)? = null
    fun setDebugSink(sink: ((String) -> Unit)?) {
        debugSink = sink
    }

    private inline fun dbg(msg: () -> String) {
        debugSink?.invoke(msg())
    }

    fun declareFunctionAnnotation(
        userFunc: UserFunctionEmbedding,
        hasPureAnnotation: Boolean,
    ) {
        declaredPure[userFunc.name!!] = hasPureAnnotation
    }

    fun checkIsPure(
        userFunc: UserFunctionEmbedding,
    ): Boolean {
        val rawBody = userFunc.body?.rawExpEmbedding ?: return false
        val key = userFunc.name!!
        val debugName = key.mangled

        // If function is annotated non-pure -> immediately impure
        if (declaredPure[key] == false) {
            registry[key] = PurityStatus.IMPURE
            dbg { "$debugName is not explicitly annotated as Pure, assuming impure!" }
            return false
        }

        // Checking the cache
        when (registry[key]) {
            PurityStatus.PURE -> return true
            PurityStatus.IMPURE -> return false
            else -> {}
        }

        // Checking whether the function is maximum one expression
        val expsCount = countRealTopLevel(rawBody)
        if (expsCount != 2) {
            registry[key] = PurityStatus.IMPURE
            dbg { "$debugName is impure: function body contains more than one top-level expression (${expsCount - 1} found)" }
            return false
        }

        // First visit – evaluate and cache
        currentEvaluatedFunction = userFunc
        val result = isExpressionPure(rawBody)
        // isExpressionPure returns true -> function is Pure
        // false -> function is incomplete (in which case the purity Status is set to checking) or the function is impure in which case the purity status has not been touched (either the entry doesn't exist or it is unknown)
        if (result) {
            registry[key] = PurityStatus.PURE
            dbg { "Purity check succeeded: $debugName is pure" }
            resolveFunctionalDependencies(userFunc.name!!)
        } else if (registry[key] == null || registry[key] != PurityStatus.CHECKING) {
            registry[key] = PurityStatus.IMPURE
            dbg { "Purity check failed: $debugName is impure (all dependencies resolved)" }
            resolveFunctionalDependencies(userFunc.name!!)
        }
        return result
    }

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
        is MethodCall -> { // we are still missing self recursion
            val isSelfCall = (exp.method.name == currentEvaluatedFunction?.name)

            if (isSelfCall) {
                exp.args.all(::isExpressionPure)
            } else {
                val mangledName = exp.method.name.mangled
                val argsPure = exp.args.all(::isExpressionPure)
                val operatorPure =
                    PureOperatorWhitelist.isWhitelisted(mangledName) && argsPure
                if (operatorPure) true
                else {
                    val callee = registry[exp.method.name]
                    when (callee) {
                        null -> {
                            registry[exp.method.name] = PurityStatus.UNKNOWN
                            addFunctionalDependency(exp.method.name)
                            registry[currentEvaluatedFunction!!.name!!] = PurityStatus.CHECKING
                            false
                        }
                        PurityStatus.CHECKING -> false
                        PurityStatus.PURE -> true
                        PurityStatus.IMPURE -> false
                        else -> false
                    }
                }
            }
        }

        // Single-expression If - just allow this for now
        is If -> isExpressionPure(exp.condition) &&
                isExpressionPure(exp.thenBranch) &&
                isExpressionPure(exp.elseBranch)

        is Goto -> true          // auto-generated return
        is InhaleDirect -> true  // auto-generated check

        // Local Variable Declarations
        is Declare -> false
        else -> false
    }

    private fun addFunctionalDependency(callee: MangledName) {
        waiting.getOrPut(callee) { mutableSetOf() }
            .add(currentEvaluatedFunction!!)

        dbg { "${currentEvaluatedFunction!!.name!!.mangled} is now waiting on ${callee.mangled} (dependency not yet resolved)" }
    }


    private fun resolveFunctionalDependencies(callee: MangledName) {
        val callers = waiting.get(callee) ?: return
        for (caller in callers) {
            val status = registry[caller.name]
            if (status == PurityStatus.PURE || status == PurityStatus.IMPURE) continue
            currentEvaluatedFunction = caller
            checkIsPure(caller)
        }
        if (registry[callee] == PurityStatus.PURE || registry[callee] == PurityStatus.IMPURE) waiting.remove(callee)
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
