import kotlin.contracts.contract
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
fun <!FUNCTION_WITH_UNVERIFIED_CONTRACT, VIPER_TEXT!>incorrectly_returns_false<!>(): Boolean {
    contract {
        returns(true)
    }
    return false
}

