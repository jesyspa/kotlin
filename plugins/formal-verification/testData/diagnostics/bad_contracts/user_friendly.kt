// Collection of functions with user-friendly warning message.

import kotlin.contracts.contract
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>non_nullable_returns_null<!>(x: Int): Int {
    contract {
        <!UNEXPECTED_RETURNED_VALUE!>returns(null)<!>
    }
    return x
}

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>returns_null<!>(): Int? {
    contract {
        <!UNEXPECTED_RETURNED_VALUE!>returnsNotNull()<!>
    }
    return null
}

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>incorrectly_returns_false<!>(): Boolean {
    contract {
        <!UNEXPECTED_RETURNED_VALUE!>returns(true)<!>
    }
    return false
}

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>incorrectly_returns_true<!>(): Boolean {
    contract {
        <!UNEXPECTED_RETURNED_VALUE!>returns(false)<!>
    }
    return true
}

