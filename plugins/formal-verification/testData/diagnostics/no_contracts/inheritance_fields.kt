open class FieldA
open class FieldB : FieldA()
class C

open class A(val fieldNotOverride: C) {
    open val fieldOverride: FieldA = FieldA()
}

open class B(override val fieldOverride: FieldB) : A(C())

fun <!VIPER_TEXT!>createB<!>() {
    val fieldB = FieldB()
    val b = B(fieldB)
    val fieldOverride = b.fieldOverride //should verify that fieldOverride == fieldB
    val fieldNotOverride = b.fieldNotOverride
}

open class FirstBFClass {
    open val x: Int = 1
}

open class NoBFClass: FirstBFClass() {
    override val x: Int
        get() = 1
}

class SecondBFClass(override val x: Int) : NoBFClass()

fun <!VIPER_TEXT!>createBFsAndNoBF<!>() {
    val fbf = FirstBFClass()
    val fbfx = fbf.x
    val nbf = NoBFClass()
    val nbfx = nbf.x
    val sbf = SecondBFClass(10)
    val sbfx = sbf.x
}

open class X(val a: Int)
class Y(a: Int) : X(0)

fun <!VIPER_TEXT!>createY<!>() {
    val y = Y(10)
    val ya = y.a
}
