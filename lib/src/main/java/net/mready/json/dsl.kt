package net.mready.json

@DslMarker
annotation class JsonDsl

@JsonDsl
interface JsonObjectDsl {
    infix fun String.value(value: String?)
    infix fun String.value(value: Number?)
    infix fun String.value(value: Boolean?)

    infix fun String.jsonArray(block: JsonArrayDsl.() -> Unit)
    infix fun String.jsonObject(block: JsonObjectDsl.() -> Unit)
}

@JsonDsl
interface JsonArrayDsl {
    fun emit(value: String?)
    fun emit(value: Number?)
    fun emit(value: Boolean?)

    fun emitArray(block: JsonArrayDsl.() -> Unit)
    fun emitObject(block: JsonObjectDsl.() -> Unit)
}