import kotlin.contracts.contract
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>returns_true<!>(): Boolean {
    contract {
        returns()
        returns(true)
    }
    return true
}

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>returns_false<!>(): Boolean {
    contract {
        returns()
        returns(false)
    }
    return false
}

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>conditional<!>(a: Boolean, b: Boolean): Boolean {
    contract {
        returns(false) implies (b)
        returns(false) implies (a && b)
    }
    return true
}