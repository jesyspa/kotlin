import org.jetbrains.kotlin.formver.plugin.*

@NeverVerify
fun <!VIPER_TEXT!>test<!>() {
    var x = 42
    // Pure
    verify(true,false, 2 <= x)
    // Both impure - purity checking will be stopped after the first statement
    <!PURITY_VIOLATION!>verify(x++<43, ++x<43)<!>
}

@NeverVerify
fun <!VIPER_TEXT!>testImpure<!>() {
    var x = 42
    <!PURITY_VIOLATION!>verify(++x<43)<!>
}