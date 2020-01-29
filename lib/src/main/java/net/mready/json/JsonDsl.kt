package net.mready.json

@DslMarker
annotation class JsonDslMarker

@JsonDslMarker
interface JsonDsl {
    fun jsonArray(block: JsonArrayDsl.() -> Unit): JsonValue
    fun jsonObject(block: JsonObjectDsl.() -> Unit): JsonValue
}

@JsonDslMarker
interface JsonObjectDsl: JsonDsl {
    infix fun String.value(value: Nothing?)
    infix fun String.value(value: String?)
    infix fun String.value(value: Number?)
    infix fun String.value(value: Boolean?)
    infix fun String.value(value: JsonValue?)

    infix fun String.jsonArray(block: JsonArrayDsl.() -> Unit)
    infix fun String.jsonObject(block: JsonObjectDsl.() -> Unit)
}

@JsonDslMarker
interface JsonArrayDsl: JsonDsl {
    object ArrayItemsCollector

    val array: ArrayItemsCollector

    operator fun ArrayItemsCollector.plusAssign(value: Nothing?) = emit(value)
    operator fun ArrayItemsCollector.plusAssign(value: String?) = emit(value)
    operator fun ArrayItemsCollector.plusAssign(value: Number?) = emit(value)
    operator fun ArrayItemsCollector.plusAssign(value: Boolean?) = emit(value)
    operator fun ArrayItemsCollector.plusAssign(value: JsonValue) = emit(value)

    fun emit(value: Nothing?)
    fun emit(value: String?)
    fun emit(value: Number?)
    fun emit(value: Boolean?)
    fun emit(value: JsonValue?)

    fun <T> Collection<T>.emitEach(block: (T) -> JsonValue) {
        forEach { emit(block(it)) }
    }
}