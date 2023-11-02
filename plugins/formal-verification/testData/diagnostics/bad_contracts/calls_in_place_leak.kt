import org.jetbrains.kotlin.formver.plugin.NeverConvert
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@NeverConvert
fun escape(f : () -> Unit) {
    // No contract means that we assume this function may leak the function object passed to it.
    f()
}

@Suppress("LEAKED_IN_PLACE_LAMBDA")
@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>invalid_calls_in_place<!>(f : () -> Unit) {
    contract {
        callsInPlace(f)
    }
    <!LAMBDA_MAY_LEAK!>escape(f)<!>
}

@Suppress("LEAKED_IN_PLACE_LAMBDA")
@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>function_object_call<!>(f: (() -> Unit) -> Unit, g: () -> Unit) {
    contract {
        callsInPlace(g)
    }
    return <!LAMBDA_MAY_LEAK!>f(g)<!>
}
