import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.verify
import org.jetbrains.kotlin.formver.plugin.postconditions

@AlwaysVerify
fun <!VIPER_TEXT!>testGreater<!>(init: Int) : Int {
    postconditions<Int> { result ->
        result > init
    }

    return init + 5
}

@AlwaysVerify
fun <!VIPER_TEXT!>testString<!>() : Char {
    postconditions<Char> { res ->
        res == 'a'
    }
    return "bab"[1]
}
