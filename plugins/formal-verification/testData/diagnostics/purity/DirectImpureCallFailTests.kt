// DirectImpureCallFailTests.kt
// ✖ All functions here are expected to be flagged as impure.

package test.purity.impurecall

import org.jetbrains.kotlin.formver.plugin.*

// Original “deliberately impure” reference
fun <!VIPER_TEXT!>randomConstant<!>(x: Int): Int = 3

@Pure @DumpExpEmbeddings
fun <!VIPER_TEXT!>avgRandomConstant<!>(x: Int): Int =
(x + randomConstant(x)) / 2      // ← calls impure helper

// New helper, also NOT marked @Pure
fun <!VIPER_TEXT!>impureHelper<!>(n: Int): Int = n * 42

@Pure @DumpExpEmbeddings
fun <!VIPER_TEXT!>usesImpureHelper<!>(n: Int): Int =
impureHelper(n) + 1              // ← direct impure call

// Transitive impurity chain
fun <!VIPER_TEXT!>secondImpure<!>(n: Int): Int = impureHelper(n) / 2

@Pure @DumpExpEmbeddings
fun <!VIPER_TEXT!>transitivelyImpure<!>(n: Int): Int =
secondImpure(n) - 3              // ← indirect impure call
