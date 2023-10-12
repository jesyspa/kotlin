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

@AlwaysVerify
fun <!VIPER_TEXT!>safe_last<!>(l: List<Int>) : Int? {
    val size = l.size
    if (size != 0) {
        return l[size - 1]
    } else {
        return null
    }
}

@AlwaysVerify
fun <!FUNCTION_WITH_UNVERIFIED_CONTRACT, VIPER_TEXT, VIPER_VERIFICATION_ERROR!>unsafe_last<!>(l: List<Int>) : Int {
    return l[l.size - 1]
}

@AlwaysVerify
fun <!VIPER_TEXT!>is_empty<!>(l: List<Int>) : Int {
    return if (!l.isEmpty()) {
        l[0]
    } else {
        1
    }
}

@AlwaysVerify
fun <!VIPER_TEXT!>nullable_list<!>(l: List<Int>?) {
    if (l != null && !l.isEmpty()) {
        val x = l[l.size - 1]
    }
}