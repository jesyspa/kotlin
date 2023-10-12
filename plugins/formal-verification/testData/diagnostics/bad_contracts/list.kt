// WITH_STDLIB

import org.jetbrains.kotlin.formver.plugin.AlwaysVerify

fun <!VIPER_TEXT!>list_declaration<!>() {
    val l1: List<Int>
    val l2: List<Int>?
    val l3: List<List<Int>>
}

@AlwaysVerify
fun <!VIPER_TEXT!>list_parameter<!>(l: List<Int>) {
    val myList = l
}

@AlwaysVerify
fun <!FUNCTION_WITH_UNVERIFIED_CONTRACT, VIPER_TEXT, VIPER_VERIFICATION_ERROR!>empty_list<!>() {
    val myList: List<Int> = emptyList()
    val s = myList[0]
}

@AlwaysVerify
fun <!FUNCTION_WITH_UNVERIFIED_CONTRACT, VIPER_TEXT, VIPER_VERIFICATION_ERROR!>nested_list<!>() {
    val nestedList: List<List<Int>> = emptyList()
    val t = nestedList[0]
    val u = t[0]
}

@AlwaysVerify
fun <!VIPER_TEXT!>list_add<!>(l: MutableList<Int>) {
    l.add(1)
    val n = l[0]
}

//@AlwaysVerify
//fun list_size(l: List<Int>) {
//    val s = l.size
//}

//fun list2() {
//    val l = listOf(1, 2, 3)
//}

//fun list4() {
//    val l: List<Int> = List(1) { it }
//}

/* TODO:
 * listOf
 * size
 * first
 * toMutable
 * sum
 * isEmpty
 * filter
 */