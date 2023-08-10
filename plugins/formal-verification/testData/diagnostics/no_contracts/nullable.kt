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
    if (x == null) {
        return null
    } else {
        return (x - 1) * (x + 1)
    }
}