import org.jetbrains.kotlin.formver.plugin.*

@DumpPurityDiagnostics
@NeverVerify
fun <!PURITY, PURITY, PURITY, PURITY, PURITY, VIPER_TEXT!>test1<!>(arg: Boolean) {
    var x = 3
    // Pure: Literals, Variables, Binary Operators
    verify(true, arg, 5 < x, 3+2 <= 5)
    // Impure: Evaluation stops after first impure expression
    verify(++x < 4, x++ < 5)
}

@DumpPurityDiagnostics
@NeverVerify
fun <!PURITY, VIPER_TEXT!>test2<!>() {
    var x = 3
    // Impure only
    verify(x++ < 5)
}