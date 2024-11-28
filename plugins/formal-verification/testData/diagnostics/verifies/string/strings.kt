// RENDER_PREDICATES

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.verify
import org.jetbrains.kotlin.formver.plugin.NeverConvert

class StringBox(val str: String)

@AlwaysVerify
fun <!VIPER_TEXT!>testType<!>(s: String) {
    verify(
        StringBox(s).str == s,
        StringBox("str").str == "str",
    )
}

@AlwaysVerify
fun <!VIPER_TEXT!>testLengthField<!>(s: String) {
    val len = s.length
    verify(StringBox("str").str.length == 3)
}

@AlwaysVerify
fun <!VIPER_TEXT!>testOps<!>(s: String) {
    val c = if (s.length > 0) s[0] else 'a'
    verify(c == 'a' || s.length > 0)
    val str = "aba"
    verify(
        str[0] == str[2],
        str[1] != str[0],
        str[1] == 'b',
    )
    verify("Kotlin" + "." + "String" == "Kotlin.String")
    val helloWorld = "Hello World" + '!'
}