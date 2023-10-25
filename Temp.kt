import kotlin.contracts.contract
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
fun incorrectly_returns_false(): Boolean {
    contract {
        returns(true)
    }
    return false
}


@OptIn(ExperimentalContracts::class)
fun non_nullable_returns_null(x: Int): Int {
    contract {
        returns(null)
    }
    return x
}
