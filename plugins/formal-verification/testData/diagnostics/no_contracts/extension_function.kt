fun Int.<!VIPER_TEXT!>inc<!>(): Int = this + 1

fun <!VIPER_TEXT!>extension_method_call<!>() {
    3.inc()
}

class Foo
fun Foo.<!VIPER_TEXT!>nothing<!>(): Unit { }
fun Foo.<!VIPER_TEXT!>nothing2<!>(a: Int): Unit { }
fun Foo.<!VIPER_TEXT!>inside<!>(): Unit {}

class Bar() {
    fun Foo.<!VIPER_TEXT!>inside<!>(): Unit { }
    fun <!VIPER_TEXT!>driver<!>() {
        val f = Foo()
        f.inside()
    }
}

class Baz() {
    fun Foo.<!VIPER_TEXT!>inside<!>(): Unit { }
    fun <!VIPER_TEXT!>driver<!>() {
        val f = Foo()
        f.inside()
    }
}


fun <!VIPER_TEXT!>extension_foo_function_call<!>() {
    val f = Foo()
    f.nothing()
    f.nothing2(42)
    f.inside()
}

fun <!VIPER_TEXT!>extension_foo_within_bar_function_call<!>() {
    val b = Bar()
    b.driver()
}

fun <!VIPER_TEXT!>extension_foo_within_baz_function_call<!>() {
    val b = Baz()
    b.driver()
}
