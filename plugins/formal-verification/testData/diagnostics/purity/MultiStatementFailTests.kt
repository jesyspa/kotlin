// MultiStatementFailTests.kt
// ✖ These functions have multiple statements; purity checker should reject them.

package test.purity.multistmt

import org.jetbrains.kotlin.formver.plugin.*

@Pure @DumpExpEmbeddings
fun <!VIPER_TEXT!>multiStmtAverage<!>(x: Int, y: Int): Int {
    val tmp = x + y   // statement 1
    return tmp / 2    // statement 2
}

@Pure @DumpExpEmbeddings
fun <!VIPER_TEXT!>earlyReturnFail<!>(x: Int): Int {
    if (x < 0) return 0   // statement 1
    return x              // statement 2
}