// SKIP_WHEN_OUT_OF_CONTENT_ROOT
package pack

@JvmInline
value class AnotherValueClass(val s: String)

typealias MyTypeAlias = AnotherValueClass

@JvmInline
value class Valu<caret>eClass(val value: MyTypeAlias)