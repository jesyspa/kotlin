import org.jetbrains.kotlin.formver.plugin.NeverConvert

@NeverConvert
inline fun <T> run(block: () -> T): T = block()

@NeverConvert
inline fun <T, R> T.run(block: T.() -> R): R = block()

@NeverConvert
inline fun <T> T.also(block: (T) -> Unit): T {
    block(this)
    return this
}

@NeverConvert
inline fun <T, R> T.let(block: (T) -> R): R = block(this)

@NeverConvert
inline fun <T> T.apply(block: T.() -> Unit): T = {
    this.block()
    return this
}

@NeverConvert
inline fun <T, R> with(receiver: T, block: T.() -> R): R = receiver.block()
