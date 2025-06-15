import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.Unique
import org.jetbrains.kotlin.formver.plugin.verify

open class A(
    open val a: Any,
    open var b: Any,
)

open class B(
    @property:Unique
    val a1: A,
    val a2: A,
)

class C(
    @property:Unique
    override var b: B?,
):A(0,0)

