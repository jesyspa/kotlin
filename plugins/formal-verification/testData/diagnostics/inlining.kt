import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.*
import kotlin.contracts.contract

inline fun <!VIPER_TEXT!>invoke<!>(f: (Int) -> Int): Int {
    val x = f(0)
    return f(x)
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