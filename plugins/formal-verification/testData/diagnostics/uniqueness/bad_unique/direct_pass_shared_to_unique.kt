// UNIQUE_CHECK
// NEVER_VALIDATE

import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.NeverVerify
import org.jetbrains.kotlin.formver.plugin.Unique

@NeverConvert
fun f(@Unique x: Int) {

}

@NeverConvert
fun use_f(y: Int) {
    f(<!UNIQUENESS_VIOLATION!>y<!>)
}
