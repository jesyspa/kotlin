import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

interface First {
    var field: Int
        get() = 0
        set(value: Int) {}
}

interface Second {
    var field: Int
}

fun takeFirst(first: First) {}
fun takeSecond(second: Second) {}

class Impl: First, Second {
    override var field: Int = 0
}

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>createImpl<!>() {
    contract {
        returns()
    }
    val impl = Impl()
    takeFirst(impl)
    takeSecond(impl)
}
