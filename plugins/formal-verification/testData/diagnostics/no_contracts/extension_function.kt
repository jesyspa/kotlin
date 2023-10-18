fun Int.<!VIPER_TEXT!>inc<!>(): Int = this + 1

fun <!VIPER_TEXT!>extension_method_call<!>() {
    3.inc()
}

class Foo
fun Foo.<!VIPER_TEXT!>return_unit<!>(): Unit { }
fun Foo.<!VIPER_TEXT!>return_unit<!>(a: Int): Unit { }
fun Foo.<!VIPER_TEXT!>inside<!>(): Unit {}

class Bar {
    fun Foo.<!VIPER_TEXT!>inside<!>(): Unit {
        val refFoo = this@Foo
        val refBar = this@Bar
        val implicitRef = this // The extension receiver (Foo) takes precedence
        refBar.inside()
    }

    fun <!VIPER_TEXT!>inside<!>(): Unit {
        // Returns Unit from Bar
    }

    fun <!VIPER_TEXT!>driver<!>() {
        val f = Foo()
        f.inside()
    }
}


fun <!VIPER_TEXT!>extension_foo_function_call<!>() {
    val f = Foo()
    f.return_unit()
    f.return_unit(42)
    f.inside()
}

fun <!VIPER_TEXT!>extension_foo_within_bar_function_call<!>() {
    val b = Bar()
    b.driver()
}

