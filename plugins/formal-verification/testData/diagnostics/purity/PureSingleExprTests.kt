// PureSingleExprTests.kt
// ✔ All these @Pure @DebugPurity functions consist of a single expression.
//   The purity checker should accept every one of them.

package test.purity.singleexpr

import org.jetbrains.kotlin.formver.plugin.*


// ── Literals ───────────────────────────────────────────────────────────────
@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>intLit<!>(x: Int): Int = 3

@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>boolLit<!>(x: Boolean): Boolean = true

@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>charLit<!>(): Char = 'A'

@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>stringLit<!>(): String = "hello"

// ── Unary operations ──────────────────────────────────────────────────────
@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>negate<!>(x: Int): Int = -x

@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>negateBoolean<!>(x: Boolean): Boolean = !x

// ── Binary operations / arithmetic / logic ────────────────────────────────
@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>avg<!>(x: Int, y: Int): Int = (x + y) / 2

@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>conjunction<!>(x: Boolean, y: Boolean): Boolean = x && y


@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>bitAnd<!>(x: Int, y: Int): Int = x and y

@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>bitShift<!>(x: Int): Int = x shl 1

// Comparison wrapped in an expression
@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>compareChars<!>(a: Char, b: Char): Int = if (a < b) 1 else 0
// ── Nested pure-function call (should pass) ───────────────────────────────

@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>avgRandomConstant2<!>(x: Int): Int = (x + pureRandomConstant2(x)) / 2

@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>pureRandomConstant2<!>(x: Int): Int = pureRandomConstant(x)/3

@Pure
@DebugPurity
//@DumpExpEmbeddings
fun <!VIPER_TEXT!>pureRandomConstant<!>(x: Int): Int = 3