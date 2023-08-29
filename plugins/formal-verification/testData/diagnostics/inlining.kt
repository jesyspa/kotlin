import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.*
import kotlin.contracts.contract

inline fun <!VIPER_TEXT!>invoke<!>(f: (Int) -> Int): Int {
    val x = f(0)
    return f(0)
}

@Suppress("WRONG_INVOCATION_KIND", "LEAKED_IN_PLACE_LAMBDA")
@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>foo<!>(g: (Int) -> Int): Int {
    contract {
        callsInPlace(g, AT_LEAST_ONCE)
    }
    val z = invoke(g)
    return invoke(g)
}

@Suppress("WRONG_INVOCATION_KIND", "LEAKED_IN_PLACE_LAMBDA")
@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>pass_lambda<!>(g: (Int) -> Unit): Int {
    contract {
        callsInPlace(g, AT_LEAST_ONCE)
    }
    return invoke { g(it); g(it); it * 2 }
}

@Suppress("WRONG_INVOCATION_KIND", "LEAKED_IN_PLACE_LAMBDA")
@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>pass_lambda_nested<!>(f: (Int) -> Int, g: (Int) -> Int): Int {
    contract {
        callsInPlace(g, AT_LEAST_ONCE)
    }
    return invoke { f(g(it)) }
}