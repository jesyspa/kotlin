// RENDER_PREDICATES

import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.Unique
import org.jetbrains.kotlin.formver.plugin.verify

open class A(
    val x: Int,
    var y: Int,
)

class B(
    @property:Unique
    val a1: A,
    val a2: A,
)

class C(
    @property:Unique
    var b: B?,
):A(0,0)

fun <!VIPER_TEXT!>testElem<!>(@Unique instance: C) {
    var abc = instance.b?.a1?.x
}

