import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import org.jetbrains.kotlin.formver.plugin.NeverConvert

fun <T> <!VIPER_TEXT!>idFun<!>(arg: T): T = arg

@NeverConvert
public inline fun <T, R> T.runWithId(block: T.() -> R): R = idFun(this).block()

@NeverConvert
public inline fun <T, R> T.copiedRun(block: T.() -> R): R = block()



class ClassWithMember(val member: Int)

@OptIn(ExperimentalContracts::class)
fun <!VIPER_TEXT!>checkMemberAccess<!>(): Boolean {
    contract {
        returns(true)
    }

    val obj = ClassWithMember(42)
    obj.runWithId {
        member
    }
    obj.copiedRun {
        return obj.member == 42
    }
}

class Box<T>(val wrapped: T)

@OptIn(ExperimentalContracts::class)
fun <T> <!VIPER_TEXT!>checkGenericMemberAccess<!>(box: Box<T>): Boolean {
    contract {
        returns(true)
    }

    box.runWithId {
        wrapped
    }

    Box(box.wrapped).copiedRun {
        return wrapped == box.wrapped
    }
}
