import kotlin.contracts.contract
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>incorrectly_returns_false<!>(): Boolean {
    contract {
        <!UNEXPECTED_RETURNED_VALUE!>returns(true)<!>
    }
    return false
}

