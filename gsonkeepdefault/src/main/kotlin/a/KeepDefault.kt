package a

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.BufferedReader
import java.lang.reflect.Type

/**
 * created by zijing on 2019/1/15
 */

@Retention(AnnotationRetention.RUNTIME)
annotation class KeepDefault(val showErrorIfNoDefault: Boolean = false)

interface IKeepDefault<T> {
    fun tryKeepDefault(originData: T?): T?
}

fun <T> IKeepDefault<T>.generateAdapter(gson: Gson, gsonFactory: TypeAdapterFactory, clz: Class<T>): TypeAdapter<T>? {
    return object : TypeAdapter<T>() {
        override fun write(out: JsonWriter?, value: T) {
            gson.getAdapter(clz).write(out, value)
        }

        override fun read(`in`: JsonReader?): T? {
            return tryKeepDefault(gson.getDelegateAdapter(gsonFactory, TypeToken.get(clz)).read(`in`))
        }
    }
}

inline fun <reified T> KGson.fromJson(j: String): T =
    fromJson(j, object : TypeToken<T>(){} .type)

inline fun <reified T> KGson.fromJson(reader: BufferedReader): T =
    fromJson(reader, object : TypeToken<T>(){} .type)

private fun <T> Class<T>.isPrimitiveField(): Boolean = primitiveClassSet.contains(this)

private val primitiveClassSet = setOf(
    Boolean::class.java,
    Int::class.java,
    Short::class.java,
    Long::class.java,
    Float::class.java,
    Double::class.java,
    String::class.java
)

interface KGson {

    companion object : KGson

    fun <T> fromJson(jsonString: String, clz: Type): T {
        return KGsonBuilder().create().fromJson(jsonString, clz)
    }

    fun <T> fromJson(bufferedReader: BufferedReader, clz: Type): T {
        return KGsonBuilder().create().fromJson(bufferedReader, clz)
    }

    fun <T> fromJson(jsonReader: JsonReader, clz: Type): T {
        return KGsonBuilder().create().fromJson(jsonReader, clz)
    }

    fun <T> toJson(instance: T): String {
        return Gson().toJson(instance)
    }

    private fun KGsonBuilder(): GsonBuilder {
        val gsonBuilder = GsonBuilder()

        val typeAdapterFactory = object : TypeAdapterFactory {
            override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
                val c = type.rawType
                if (c.isPrimitive || c.isPrimitiveField() // primitive fields
                    || c.canonicalName.contains(Regex("^(java|kotlin).*")) // system package
                    || c.`package` == null // primitive arrays
                ) {
                    return null
                }
                val packageName = c.`package`.name
                val simpleNames = c.canonicalName.substring(packageName.length).split(".").filter { it.isNotEmpty() }
                val helperClassName = "KeepDefaultHelper_${simpleNames.joinToString("_")}"
                return try {
                    (Class.forName("$packageName.$helperClassName")
                        .getField("INSTANCE").get(null) as? IKeepDefault<Any>)?.let {
                        it.generateAdapter(gson, this, c as Class<Any>) as? TypeAdapter<T>
                    }
                } catch (e: java.lang.Exception) {
                    null
                }
            }
        }
        gsonBuilder.registerTypeAdapterFactory(typeAdapterFactory)
        return gsonBuilder
    }
}
