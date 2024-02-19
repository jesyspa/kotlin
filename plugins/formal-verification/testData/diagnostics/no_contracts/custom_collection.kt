// WITH_STDLIB

class IntEntry(override val key: Int, override val value: Int): Map.Entry<Int, Int>

// demonstrates that two different 'size' fields are indeed created
class OneValueMap(singleValue: Int, override val size: Int): AbstractMap<Int, Int>() {
    override val entries: Set<Map.Entry<Int, Int>> = (0 until size).map { IntEntry(it, singleValue) }.toSet()
}

fun <!VIPER_TEXT!>createOneValueMap<!>() {
    val oneValueMap = OneValueMap(4, 4)
}