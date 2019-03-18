## gsonkeepdefault

This is a pure kotlin library used together with [Gson](https://github.com/google/gson) in the scenarios of decoding JSON data with a `data class` in Kotlin.

## Background

If we have code like this:

```kotlin
data class AValue(val a: String = "keep me")
val a1 = Gson.fromJson("""{"a":null}""", AValue::class.java)
print(a1.a) // null
```

This happens often during developing, since Kotlin can do no assurance on JVM runtime.

However, what we need is pretty simple: we want our codes run as they seem like. So I write this `gsonkeepdefault` library to help you keep the default values of `data class` in development.

## How To Use

### Dependency (Gradle)

```groovy
repositories {
  maven { url "https://jitpack.io" }
}

apply plugin 'kotlin-kapt'

dependencies {
  implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
  implementation "com.github.zijing07.gsonkeepdefault:core:1.1"
  kapt "com.github.zijing07.gsonkeepdefault:processor:1.1"
}
```

### Dependency (Kotlin DSL Build Script)

```groovy
plugins {
  kotlin("jvm")
  kotlin("kapt")
}

repositories {
  maven(url = "https://jitpack.io")
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation("com.github.zijing07.gsonkeepdefault:core:1.1")
  kapt("com.github.zijing07.gsonkeepdefault:processor:1.1")
}
```

### Kotlin

```kotlin
@KeepDefault
data class AValue(val a: String = "keep me")
val a1 = KGson.fromJson<AValue>("""{"a":null}""")
print(a1.a) // "keep me"
```

Please note that you need to use `KGson` here instead of `Gson`.

## Related Libraries

- [Moshi](https://github.com/square/moshi) Innovated me on the idea
- [Kotlin Metadata](https://github.com/Takhion/kotlin-metadata) Really excellent work, does help to extract the `default` modifier info from a `data class`
- [Kotlin Poet](https://github.com/square/kotlinpoet) A really good poet to be a parter of annotation processors
