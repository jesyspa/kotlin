import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>compoundConditionalEffect<!>(b: Boolean): Unit {
    contract {
        <!CONDITIONAL_EFFECT_ERROR!>returns() implies (b && false)<!>
    }
}

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>mayReturnNonNull<!>(x: Any?): Any? {
    contract {
        <!CONDITIONAL_EFFECT_ERROR!>returns(null) implies (x is Int)<!>
    }
    return x
}

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>mayReturnNull<!>(x: Any?): Any? {
    contract {
        <!CONDITIONAL_EFFECT_ERROR!>returnsNotNull() implies (x is Int)<!>
    }
    return x
}

/**
 * This is a wrong version of [CharSequence?.isNullOrEmpty] from the standard library.
 * The function return statement has been negated.
 */
@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>isNullOrEmptyWrong<!>(seq: CharSequence?): Boolean {
    contract {
        <!CONDITIONAL_EFFECT_ERROR!>returns(false) implies (seq != null)<!>
    }
    return seq != null && seq.length != 0
}

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>recursiveContract<!>(n: Int, x: Any?): Boolean {
    contract {
        <!CONDITIONAL_EFFECT_ERROR!>returns(true) implies (x is String)<!>
    }
    return if (n == 0) x is Int else recursiveContract(n - 1, x)
}