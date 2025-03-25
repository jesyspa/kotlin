import org.jetbrains.kotlin.formver.plugin.*

@AlwaysVerify
fun <!VIPER_TEXT!>resultOrArg<!>(arg: Boolean): Boolean {
    postconditions<Boolean> { res ->
        res || arg
    }
    return !arg
}

@AlwaysVerify
fun <!VIPER_TEXT!>testImplies<!>(arg: Boolean): Boolean {
    postconditions<Boolean> { res ->
        arg then !res
        res then !arg
        !arg then res
        !res then arg
    }
    return !arg
}

@AlwaysVerify
fun <!VIPER_TEXT!>testAnd<!>(arg1: Boolean, arg2: Boolean): Boolean {
    postconditions<Boolean> { res ->
        res then (arg1 && arg2)
        !res then (!arg1 || !arg2)
        (arg1 && arg2) then res
        !arg1 then !res
        !arg2 then !res
    }
    return arg1 && arg2
}