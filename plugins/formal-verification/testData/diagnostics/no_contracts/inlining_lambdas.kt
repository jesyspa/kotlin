inline fun <!VIPER_TEXT!>invoke<!>(f: (Int) -> Int): Int {
    return f(0)
}

fun <!VIPER_TEXT!>explicit_arg<!>(): Int {
    return invoke { x -> x + x }
}

fun <!VIPER_TEXT!>implicit_arg<!>(): Int {
    return invoke { it * 2 }
}

fun <!VIPER_TEXT!>lambda_if<!>(): Int {
    return invoke {
        if (it == 0) {
            it + 1
        } else {
            it + 2
        }
    }
}

fun <!VIPER_TEXT!>return_value_not_used<!>(): Unit {
    invoke { it }
}