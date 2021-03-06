package me.mozidev.keepdefault

import com.google.gson.stream.JsonReader
import org.intellij.lang.annotations.Language
import org.junit.Test
import java.io.BufferedReader
import java.io.StringReader

/**
 * created by zijing on 2019/1/21
 */
class KeepDefaultProcessorTest {
    @KeepDefault
    data class AValue(val a: String = "-a")

    @KeepDefault
    data class BValue(
        val b: String = "-b",
        val av: AValue = AValue("da")
    )

    @KeepDefault
    data class CValue(
        val c: String = "-c",
        val dv: DValue
    ) {
        @KeepDefault
        data class DValue(
            val d: String = "-d",
            val m: Int = 1
        )
    }

    @Test
    fun testDefaultValue() {

        @Language("JSON")
        val json = """{"a":"aaa"}"""

        val a1 = KGson.fromJson<AValue>(json)
        assert(a1.a == "aaa")
    }

    @Test
    fun testDefaultValueWithNull() {
        @Language("JSON")
        val json = """{"a":null}"""

        val a1 = KGson.fromJson<AValue>(json)
        assert(a1.a == "-a")
    }

    @Test
    fun testNestedClassDefaultValue() {
        @Language("JSON")
        val json = """{"b": null, "av": null}"""

        val b: BValue = KGson.fromJson(json)
        assert(b.b == "-b")
        assert(b.av == AValue("da"))
    }

    @Test
    fun testNestedClassDefaultValue2() {
        @Language("JSON")
        val json = """{"b": null, "av": {"a": null}}"""

        val b = KGson.fromJson<BValue>(json)

        assert(b.b == "-b")
        assert(b.av.a == "-a")
    }

    @Test
    fun testInnerStaticClass() {
        @Language("JSON")
        val json = """{"dv": {"m": null}}"""

        val c = KGson.fromJson<CValue>(json)

        val target = CValue(dv = CValue.DValue())
        assert(c == target)
    }

    @Test
    fun testList() {
        @Language("JSON")
        val json = """[{"a": null}, {"a": null}]"""

        val al = KGson.fromJson<List<AValue>>(json)
        assert(al == listOf(AValue(), AValue()))
    }

    @Test
    fun testJsonReader() {
        @Language("JSON")
        val json = """{"a":"aaa"}"""

        val jsonReader = JsonReader(StringReader(json))
        val a1 = KGson.fromJson<AValue>(jsonReader, AValue::class.java)
        assert(a1.a == "aaa")
    }

    @Test
    fun testBufferReader() {
        @Language("JSON")
        val json = """{"a":"aaa"}"""

        val bufferReader = BufferedReader(StringReader(json))
        val a = KGson.fromJson<AValue>(bufferReader)
        assert(a.a == "aaa")
    }

    @Test
    fun testToJson() {
        @Language("JSON")
        val targetJson = """{"a":"aaa"}"""

        val a = AValue("aaa")
        val json = KGson.toJson(a)
        assert(json == targetJson)
    }
}
