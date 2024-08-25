import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.verify
import org.jetbrains.kotlin.formver.plugin.NeverConvert

class ClassWithExtension(val delta: Int) {
    @AlwaysVerify
    fun Int.<!VIPER_TEXT!>applyDelta<!>() {
        val withoutLabels = this + delta
        val withLabels = this@ClassWithExtension.delta + this@applyDelta
        verify(withoutLabels == withLabels)
    }
    fun <!VIPER_TEXT!>returnDelta<!>() = delta
}

fun ClassWithExtension.<!VIPER_TEXT!>extensionReturnDelta<!>() = delta

@NeverConvert
public inline fun <T, R> T.copiedRun(block: T.() -> R): R = block()

@AlwaysVerify
fun <!VIPER_TEXT!>checkClassWithExtension<!>() {
    ClassWithExtension(42).copiedRun {
        42.applyDelta()
        returnDelta()
        extensionReturnDelta()
    }
}


