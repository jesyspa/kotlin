import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.NeverVerify
import org.jetbrains.kotlin.formver.plugin.Unique

@NeverConvert
@NeverVerify
fun f(x: Int) {

}

@NeverConvert
@NeverVerify
fun use_f(y: Int) {
    f(y)
}
