// MAIN_FILE_NAME: MultifileClass__File2Kt
// DECLARATION_TYPE: org.jetbrains.kotlin.psi.KtNamedFunction
// COMPILER_ARGUMENTS: -Xmultifile-parts-inherit

// FILE: file1.kt
@file:JvmMultifileClass
@file:JvmName("MultifileClass")
package one

private fun privateFunction1() {}

// FILE: file2.kt
@file:JvmMultifileClass
@file:JvmName("MultifileClass")
package one

private fun privateFunction2() {}
