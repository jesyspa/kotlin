import org.jetbrains.kotlin.formver.plugin.AlwaysVerify

open class A(val a: Int)

open class B(val b: Int) : A(0)

interface D {
    val d: Int
        get() = 2
}

class C(val x: A, var y: A) : D, B(0)

@AlwaysVerify
fun <!VIPER_TEXT!>accessSuperTypeProperty<!>(c: C){
    val temp = c.a
}

@AlwaysVerify
fun <!VIPER_TEXT!>accessNested<!>(c: C){
    val temp = c.x.a
}

@AlwaysVerify
fun <!VIPER_TEXT!>accessNullable<!>(x: A?){
    var n: Int
    if (x != null) {
        n = x.a
    }
}