import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import org.jetbrains.kotlin.formver.plugin.NeverConvert

fun <T> <!VIPER_TEXT!>idFun<!>(arg: T): T = arg

@NeverConvert
public inline fun <T, R> T.runWithId(block: T.() -> R): R = idFun(this).block()

class ClassWithMember {
    val member = 42
}

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>checkMemberAccess<!>(): Int {
    contract {
        returns()
    }
    return ClassWithMember().runWithId {
        member
    }
}