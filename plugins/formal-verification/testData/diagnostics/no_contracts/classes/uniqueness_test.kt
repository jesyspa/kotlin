// RENDER_PREDICATES

import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.Unique
import org.jetbrains.kotlin.formver.plugin.verify


class A(var x: Int)

fun f(@Unique a: A) {
  val i = a.x
  val j = a.x
  verify(i == j)
}

class B(@property:Unique var a: A)

fun <!VIPER_TEXT!>g<!>(@Unique b: B) {
  val i = b.a.x
  val j = b.a.x
  verify(i == j)
}