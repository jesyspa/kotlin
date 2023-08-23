fun <!VIPER_TEXT!>while_loop<!>(b: Boolean): Int {
    while (b) {
        val a = 1
        val c = 2
    }
    return 0
}

fun <!VIPER_TEXT!>returns_bool<!>(): Boolean {
    return false
}

fun <!VIPER_TEXT!>while_function_condition<!>() {
    while (returns_bool()) {

    }
}
