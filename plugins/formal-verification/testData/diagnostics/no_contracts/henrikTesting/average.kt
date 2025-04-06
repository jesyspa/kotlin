import org.jetbrains.kotlin.formver.plugin.AlwaysVerify
import org.jetbrains.kotlin.formver.plugin.verify
import org.jetbrains.kotlin.formver.plugin.NeverConvert
import org.jetbrains.kotlin.formver.plugin.NeverVerify
import org.jetbrains.kotlin.formver.plugin.DumpExpEmbeddings

@DumpExpEmbeddings
@NeverVerify0
fun <!EXP_EMBEDDING, VIPER_TEXT!>average<!>(x: Int, y: Int): Int = (x + y) / 2