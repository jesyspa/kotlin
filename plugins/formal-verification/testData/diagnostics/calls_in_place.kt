import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.*
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>exactly_one<!>(f : (Int) -> Int) : Int{
    contract {
        callsInPlace(f, EXACTLY_ONCE)
    }
    return f(1)
}