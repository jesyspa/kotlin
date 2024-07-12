// REPLACE_STDLIB_EXTENSIONS

import kotlin.contracts.contract
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>useRepeat<!>(): Unit {
    contract {
        returns()
    }
    check(true)
    check(true) { "Lazy message" }
}

// TODO: add test for `repeat` (we actually have a bug there because we require unsatisfied precondition in loops)

// TODO: add tests for `let`, `apply`, `with`, `run`, `also`
