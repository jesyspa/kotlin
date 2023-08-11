fun return_nullable(): Int? {
    return 0
}

fun smart_cast(x: Int?): Int {
    if (x == null) {
        return 0
    } else {
        return x
    }
}

fun use_nullable_twice(x: Int?): Int? {
    val a = x
    val b = x
    return a
}

fun pass_nullable_parameter(x: Int?): Int? {
    use_nullable_twice(x)
    return x
}