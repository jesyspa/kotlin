import org.jetbrains.kotlin.formver.plugin.NeverConvert

@NeverConvert
fun call(x: Int) {
}

fun <!VIPER_TEXT!>try_catch<!>() {
    try {
        call(0)
        call(1)
    } catch (e: Exception) {
        call(2)
    }
}

@NeverConvert
@Suppress("NOTHING_TO_INLINE")
inline fun call_twice() {
    call(0)
    call(1)
}

fun <!VIPER_TEXT!>try_catch_with_inline<!>() {
    try {
        call_twice()
    } catch (e: Exception) {
        call(2)
    }
}