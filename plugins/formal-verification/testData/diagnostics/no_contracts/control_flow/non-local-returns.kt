// NEVER_VALIDATE

import org.jetbrains.kotlin.formver.plugin.NeverConvert

@NeverConvert
inline fun invoke(f: (Int) -> Int): Int {
    return f(0)
}

fun <!VIPER_TEXT!>simpleReturn<!>(): Int {
    return invoke {
        return 1
        it
    }
}

fun <!VIPER_TEXT!>returnAtInline<!>(): Int {
    return invoke {
        return@invoke 1
        it
    }
}

fun <!VIPER_TEXT!>doubleInvoke<!>(): Int {
    return invoke {
        invoke {
            return@invoke 1
            it
        }
        return@invoke 2
        it
    }
}


@NeverConvert
inline fun invoke2(f: (Int) -> Int): Int {
    return f(1)
}

fun <!VIPER_TEXT!>nested<!>(): Int {
    return invoke {
        invoke2 {
            return@invoke2 2
            return@invoke 3
            return 4
            it
        }
        return@invoke 5
        it
    }
}