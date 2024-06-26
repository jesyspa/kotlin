// LANGUAGE: +JvmFieldInInterface +NestedClassesInAnnotations
// TARGET_BACKEND: JVM

// WITH_STDLIB
// FILE: Foo.kt

public class Bar(public val value: String)

annotation class Foo {
    companion object {
        @JvmField
        val FOO = Bar("OK")
    }
}


// FILE: bar.kt

fun box(): String {
    return Foo.FOO.value
}
