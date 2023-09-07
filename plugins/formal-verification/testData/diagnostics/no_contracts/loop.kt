fun <!VIPER_TEXT!>while_loop<!>(b: Boolean): Boolean {
    while (b) {
        val a = 1
        val c = 2
    }
    return false
}

fun <!VIPER_TEXT!>while_function_condition<!>() {
    while (while_loop(true)) { }
}

fun <!VIPER_TEXT!>while_break<!>(b: Boolean): Int {
    var i = 0
    while (b) {
        i = 1
        break
    }
    return i
}

fun <!VIPER_TEXT!>while_continue<!>(b: Boolean): Int {
    var i = 0
    while (b) {
        i = 1
        continue
    }
    return i
}

fun <!VIPER_TEXT!>while_nested<!>(b: Boolean) {
    while(b){
        while (b){
            break
        }
        continue
        while (b){
            continue
        }
        break
    }
}