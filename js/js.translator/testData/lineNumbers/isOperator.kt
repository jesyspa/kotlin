var log = ""

fun box(a: Any) {
    if (a is String && foo()) {
        log += "OK"
    }
}

inline fun foo(): Boolean {
    log += "foo"
    return true
}

// LINES(JS_IR): 1 1 1 1 1 1 1 1 * 3 3 * 4 4 * 10 10 11 4 4 4 5 5 9 9 10 10 11 11 * 1
