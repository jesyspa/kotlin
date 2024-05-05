import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import org.jetbrains.kotlin.formver.plugin.NeverVerify

interface First {
    val field: Int
        get() {
            "Computing..."
            return 0
        }
}

interface Second {
    val field: Int
}

abstract class Third {
    val field: Int = 0
}

@NeverVerify
fun <!VIPER_TEXT!>takeFirst<!>(first: First) {
    first.field
}

@NeverVerify
fun <!VIPER_TEXT!>takeSecond<!>(second: Second) {
    second.field
}

@NeverVerify
fun <!VIPER_TEXT!>takeThird<!>(third: Third) {
    third.field
}

class Impl12: First, Second {
    override val field: Int = 0
}

class Impl3: Third()
class Impl23: Second, Third()

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>createImpls<!>(): Boolean{
    contract {
        returns(false) implies false
    }
    val impl12 = Impl12()
    val start12 = impl12.field
    takeFirst(impl12)
    takeSecond(impl12)
    val impl23 = Impl23()
    val start23 = impl23.field
    takeSecond(impl23)
    takeThird(impl23)
    val impl3 = Impl3()
    val start3 = impl3.field
    takeThird(impl3)
    return start12 == impl12.field && start23 == impl23.field && start3 = impl3.field
}

