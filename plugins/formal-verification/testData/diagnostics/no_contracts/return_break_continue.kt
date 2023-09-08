fun <!VIPER_TEXT!>test_return<!>(): Int {
    return 0
    return 1
}

fun <!VIPER_TEXT!>return_from_loop<!>(): Int {
    while (true) {
        return 0
    }
    return 1
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