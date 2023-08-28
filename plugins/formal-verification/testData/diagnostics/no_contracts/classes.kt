class Foo(val a: Int, val b: Int) {
    val sum: Int get() { return a + b }
}

fun createFoo() {
    val f: Foo = Foo(10, 20)
    val fa = f.a
    val fb = f.b
}