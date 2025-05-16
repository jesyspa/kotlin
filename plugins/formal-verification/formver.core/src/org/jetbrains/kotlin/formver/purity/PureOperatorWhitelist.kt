// PureOperatorWhitelist.kt
package org.jetbrains.kotlin.formver.purity

/**
 * Utility that decides whether a *mangled* method name represents
 * a primitive operator we always treat as pure.
 */
object PureOperatorWhitelist {

    /** Substrings that are guaranteed to appear in the mangled spelling
     *  of the corresponding operator functions. Extend as needed. */
    private val TOKENS: Set<String> = setOf(
        // unary
        "unaryMinus", "unaryPlus", "not",
        // binary arithmetic & bitwise
        "plus", "minus", "times", "div", "rem",
        "and", "or", "xor", "shl", "shr", "ushr",
        // comparison / equals
        "equals", "compareTo"
    )

    /**
     * @param mangled fully-mangled method name (e.g. "plus$T$Int$T$Int")
     * @return true  ⇒ treat call as inherently pure (assuming args are pure)
     */
    fun isWhitelisted(mangled: String): Boolean =
        TOKENS.any { token -> mangled.contains(token) }
}
