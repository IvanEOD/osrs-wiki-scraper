package org.tribot.wikiscraper.lua

import org.tribot.wikiscraper.utility.getNonNullable


/* Written by IvanEOD 12/13/2022, at 12:40 PM */

class LuaScript {

    val requiredModules = mutableListOf<String>()
    val requiredFunctions = mutableListOf<String>()

    val script = StringBuilder()

    private fun addCode(code: String) {
        script.append("\n\n$code\n\n")
    }

    fun addModule(module: String) {
        if (module !in requiredModules) requiredModules.add(module)
    }

    fun addFunction(function: String) {
        if (function !in requiredFunctions) requiredFunctions.add(function)
    }


    companion object {

    }

}

enum class LuaType(val luaTypeString: kotlin.String, val isJvmType: (kotlin.String) -> kotlin.Boolean) {

    Any("any", { it in listOf("Any", "Object") }),
    String("string", { it in listOf("String") }),
    Number("number", { it in listOf("Number", "Int", "Long", "Float", "Double") }),
    Boolean("boolean", { it in listOf("Boolean") }),
    Table("table", {
        it in listOf("Map", "MutableMap", "HashMap", "SingletonMap", "LinkedHashMap", "ArrayList", "List", "Array")
                || it.endsWith("[]")
    }),
    Function("function", { it in listOf("Method") }),
    Nil("nil", { it in listOf("null", "Nothing", "Unit") })
    ;

    companion object {
        fun fromLuaTypeString(luaTypeString: kotlin.String): LuaType {
            return LuaType.values().first { it.luaTypeString == luaTypeString }
        }

        fun fromJvmTypeString(jvmTypeString: kotlin.String): LuaType {
            return LuaType.values().first { it.isJvmType(jvmTypeString) }
        }

        fun from(value: kotlin.Any?): LuaType =
            fromJvmTypeString(if (value == null) "null" else value::class.java.simpleName)

        fun hasLuaType(value: kotlin.Any?) = runCatching { from(value) }.isSuccess

    }


}

sealed interface LuaHandle {
    val name: String
    val description: String
    val dependencies: List<LuaHandle>
    val arguments: List<LuaTypeRequirement>
}

data class LuaFunction(
    override val name: String,
    override val description: String = "",
    override val dependencies: List<LuaHandle> = emptyList(),
    override val arguments: List<LuaTypeRequirement> = emptyList(),
    val returns: List<LuaTypeRequirement> = emptyList()
) : LuaHandle

data class LuaModule(
    override val name: String,
    override val description: String = "",
) : LuaHandle {
    override val dependencies: List<LuaHandle> = emptyList()
    override val arguments: List<LuaTypeRequirement> = emptyList()
}

sealed class LuaVariable(
    override val name: String,
    override val description: String,
    val value: LuaPrimitive,
    val local: Boolean = false
) : LuaHandle {
    override val dependencies: List<LuaHandle> = emptyList()
    override val arguments: List<LuaTypeRequirement> = listOf()

    companion object {

    }

}

sealed interface LuaTypeRequirement {
    fun accepts(type: LuaPrimitive): Boolean
}

data class StrictTypeRequirement(val type: LuaType) : LuaTypeRequirement {
    override fun accepts(type: LuaPrimitive): Boolean = this.type == type.type
}

data class LenientTypeRequirement(val types: List<LuaType>) : LuaTypeRequirement {
    override fun accepts(type: LuaPrimitive): Boolean = type.type in types
}

object AnyTypeRequirement : LuaTypeRequirement {
    override fun accepts(type: LuaPrimitive): Boolean = true
}


sealed interface LuaPrimitive {
    val type: LuaType
    fun toLua(): String
}

sealed interface KeyableLuaPrimitive : LuaPrimitive {
    fun toLuaKey(): String = "[$this.toLua()]"
}

inline fun <reified T: Any> isValidLuaKeyType(): Boolean = when (T::class) {
    String::class,
    Number::class,
    Boolean::class -> true
    else -> false
}
fun String.toLuaPrimitive(): KeyableLuaPrimitive = LuaString.fromJvm(this)
fun Number.toLuaPrimitive(): KeyableLuaPrimitive = LuaNumber.fromJvm(this)
fun Boolean.toLuaPrimitive(): KeyableLuaPrimitive = LuaBoolean.fromJvm(this)
inline fun <reified T: Any> Collection<T>.toLuaPrimitive(): LuaPrimitive = LuaTable.fromJvm(this)
inline fun <reified K: Any, reified V: Any> Map<K, V>.toLuaPrimitive(): LuaPrimitive = LuaTable.fromJvm(this)
inline fun <reified T: Any> T.toLuaKey(): KeyableLuaPrimitive = when (this) {
    is String -> this.toLuaPrimitive()
    is Number -> this.toLuaPrimitive()
    is Boolean -> this.toLuaPrimitive()
    else -> throw IllegalArgumentException("Cannot convert ${T::class.simpleName} to Lua key")
}
inline fun <reified T: Any> T.toLuaPrimitive(): LuaPrimitive {
    return LuaAny(when (this) {
        is String -> toLuaPrimitive()
        is Number -> toLuaPrimitive()
        is Boolean -> toLuaPrimitive()
        else -> throw IllegalArgumentException("Cannot convert ${this::class.java.simpleName} to LuaPrimitive")

    })

}

fun String?.toLuaPrimitiveOrNull(): LuaPrimitive = this?.toLuaPrimitive() ?: LuaNull
fun Number?.toLuaPrimitiveOrNull(): LuaPrimitive = this?.toLuaPrimitive() ?: LuaNull
fun Boolean?.toLuaPrimitiveOrNull(): LuaPrimitive = this?.toLuaPrimitive() ?: LuaNull
fun Collection<Any>?.toLuaPrimitiveOrNull(): LuaPrimitive = this?.toLuaPrimitive() ?: LuaNull
fun Map<Any, Any>?.toLuaPrimitiveOrNull(): LuaPrimitive = this?.toLuaPrimitive() ?: LuaNull

object LuaNull : LuaPrimitive {
    override val type: LuaType = LuaType.Nil
    override fun toLua(): String = "nil"
}

class LuaAny constructor(val value: LuaPrimitive) : LuaPrimitive by value {
    override val type: LuaType = LuaType.Any

    companion object {
        inline fun <reified T: Any> fromJvm(value: T): LuaAny = LuaAny(value.toLuaPrimitive())

        inline fun <reified T: Any> fromLua(value: String, default: T?): T {
            return when (T::class) {
                String::class -> value as T
                Number::class -> LuaNumber.fromLua(value, default as? Number) as T
                Boolean::class -> LuaBoolean.fromLua(value, default as? Boolean) as T
                Collection::class -> LuaTable.fromLua(value, default as? Collection<*>) as T
                Map::class -> LuaTable.fromLua(value, default as? Map<*, *>) as T
                else -> throw IllegalArgumentException("Cannot convert Lua type to ${T::class.simpleName}")
            }
        }
    }

}

class LuaString private constructor(var value: String) : KeyableLuaPrimitive {
    override val type: LuaType = LuaType.String

    override fun toLua(): String = "\"$value\""

    companion object {
        fun fromJvm(value: String): LuaString = LuaString(value)
        fun fromLua(value: String): String = value
    }

}

data class LuaNumber(var value: Number) : KeyableLuaPrimitive {
    override val type: LuaType = LuaType.Number
    override fun toLua(): String = value.toString()
    companion object {
        fun fromJvm(value: Number): LuaNumber = LuaNumber(value)
        fun fromLua(value: String, default: Number?): Number {
            return if (value.contains(".")) value.toDoubleOrNull() ?: value.toFloatOrNull() ?: default ?: 0
            else {
                val cleaned = value.replace("\\D", "")
                cleaned.toIntOrNull() ?: cleaned.toLongOrNull() ?: default ?: 0
            }
        }
    }
}

data class LuaBoolean(var value: Boolean) : KeyableLuaPrimitive {
    override val type: LuaType = LuaType.Boolean
    override fun toLua(): String = value.toString()
    companion object {
        fun fromJvm(value: Boolean): LuaBoolean = LuaBoolean(value)
        fun fromLua(value: String, default: Boolean?): Boolean = value.getNonNullable(default)
    }
}

class LuaTable constructor(val value: Map<KeyableLuaPrimitive, LuaPrimitive>) : LuaPrimitive {
    override val type: LuaType = LuaType.Table
    override fun toLua(): String {
        if (value.isEmpty()) return "{}"
        if (value.keys.all { it is LuaNumber }) return value.values.joinToString(",", "{", "}") { it.toLua() }
        return "{${value.map { "${it.key.toLuaKey()}=${it.value.toLua()}" }.joinToString(",")}}"
    }
    constructor(value: Collection<LuaPrimitive>) : this(value.mapIndexed { index, luaPrimitive ->
        LuaNumber(index) to luaPrimitive
    }.toMap())

    companion object {
        inline fun <reified K: Any, reified V: Any> fromJvm(value: Map<K, V>): LuaTable {
            if (isValidLuaKeyType<K>()) throw IllegalArgumentException("Cannot convert ${K::class.simpleName} to Lua key type")
            return LuaTable(value.map { it.key.toLuaKey() to it.value.toLuaPrimitive() }.toMap())
        }
        inline fun <reified T: Any> fromJvm(value: Collection<T>): LuaTable = LuaTable(value.map { it.toLuaPrimitive() })

        fun fromLua(value: String, default: Map<*, *>?): Map<*, *> {
            TODO()
        }
        fun fromLua(value: String, default: Collection<*>?): Collection<*> {
            TODO()
        }
    }

}



fun main() {

    fun printClassName(obj: Any?) {
        println(obj?.javaClass?.simpleName)
    }

    printClassName(1)
    printClassName(1.0)
    printClassName(1f)
    printClassName(1L)
    printClassName("1")
    printClassName(true)
    printClassName(mutableMapOf("1" to 1, "2" to "2"))
    printClassName(null)
    printClassName(Unit)
    printClassName(mutableListOf(1, 2, 3))
    printClassName(arrayOf(1, 3, 4))


}