import org.jetbrains.kotlin.formver.plugin.*

fun <!VIPER_TEXT!>firstNotSortedIndex<!>(s: String): Int {
    postconditions<Int> { res ->
        0 <= res && res <= s.length
        forAll<Int> {
            (0 <= it && it + 1 < res) implies (s[it] <= s[it + 1])
        }
    }

    if (s.length == 0) {
        return 0
    } else {
        var i = 1
        while (i < s.length && s[i - 1] <= s[i]) {
            loopInvariants {
                forAll<Int> {
                    (0 <= it && it + 1 < i) implies (s[it] <= s[it + 1])
                }
            }
            ++i
        }
        return i
    }
}