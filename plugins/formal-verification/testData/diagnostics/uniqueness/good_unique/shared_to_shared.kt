// UNIQUE_CHECK
// NEVER_VALIDATE

import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.NeverVerify
import org.jetbrains.kotlin.formver.plugin.Unique

fun f(x: Int) {

}

fun use_f(y: Int) {
    f(y)
}
