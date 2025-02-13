import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.verify
import org.jetbrains.kotlin.formver.plugin.preconditions

@AlwaysVerify
fun <!VIPER_TEXT!>test<!>(idx: Int) {
    preconditions {
        0 <= idx
        idx < 3
    }
    verify(0 <= idx && idx < 3)
    verify("aaa"[idx] == 'a')
}
