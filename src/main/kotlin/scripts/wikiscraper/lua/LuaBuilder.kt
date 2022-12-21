package scripts.wikiscraper.lua

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/* Written by IvanEOD 12/10/2022, at 6:27 PM */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@DslMarker
annotation class LuaMarker

typealias LuaTableBuilder = LuaTableScope.() -> Unit

private val LuaDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
private fun toLuaKeyString(key: Any): String = if (key is CustomLuaValue) key.toLuaString() else "[${toLuaValueString(key)}]"
private fun toLuaValueString(value: Any): String = when (value) {
    is CustomLuaValue -> value.toLuaString()
    is String -> "\"$value\""
    is Date -> "\"${LuaDateFormat.format(value)}\""
    is Boolean,
    is Number -> value.toString()
    is Map<*, *> -> value.entries.joinToString(",", "{", "}") {
            (k, v) -> "${toLuaKeyString(k!!)}=${toLuaValueString(v!!)}"
    }
    is List<*> -> value.joinToString(",", "{", "}") { toLuaValueString(it!!) }
    else -> throw IllegalArgumentException("Cannot convert $value to Lua")
}

@DslMarker
annotation class LocalScopeMarker


fun interface CustomLuaValue {
    fun toLuaString(): String
}

@LocalScopeMarker
interface LocalScope {

    fun String.local() = "local $this"

}

@LuaMarker
sealed interface LuaScope {
    val scope: LuaScope

    infix fun String.`=`(value: Date) : LuaScope = privateSet(this, value)
    infix fun String.`=`(value: Boolean) : LuaScope = privateSet(this, value)
    infix fun String.`=`(value: Number) : LuaScope = privateSet(this, value)
    infix fun String.`=`(value: String) : LuaScope = privateSet(this, value)
    infix fun String.`=`(value: Map<*, *>) : LuaScope = privateSet(this, value)
    infix fun String.`=`(value: Iterable<*>) : LuaScope = privateSet(this, value)
    infix fun String.`=`(value: CustomLuaValue) : LuaScope = privateSet(this, value)
    infix fun String.`=`(value: LuaTableScope.() -> Unit): LuaScope = privateSet(this, value)

    fun String.ref() = CustomLuaValue { this }

    operator fun String.unaryPlus(): LuaScope

    fun toLua(): String


}

interface LuaGlobalScope: LuaScope, LocalScope {
    override val scope: LuaGlobalScope
        get() = this

    operator fun File.unaryPlus(): LuaGlobalScope {
        if (!exists()) throw IllegalArgumentException("File $this does not exist")
        val code = readText()
        +code
        return this@LuaGlobalScope
    }

    fun require(module: String) = CustomLuaValue { "require(\"$module\")" }

}

sealed interface LuaTableScope: LuaScope {

    override val scope: LuaTableScope

    infix fun Date.`=`(value: Date) : LuaTableScope = privateSet(this, value)
    infix fun Date.`=`(value: Boolean) : LuaTableScope = privateSet(this, value)
    infix fun Date.`=`(value: Number) : LuaTableScope = privateSet(this, value)
    infix fun Date.`=`(value: String) : LuaTableScope = privateSet(this, value)
    infix fun Date.`=`(value: Map<*, *>) : LuaTableScope = privateSet(this, value)
    infix fun Date.`=`(value: Iterable<*>) : LuaTableScope = privateSet(this, value)
    infix fun Date.`=`(value: LuaTableBuilder): LuaTableScope = privateSet(this, value)
    infix fun Boolean.`=`(value: Date) : LuaTableScope = privateSet(this, value)
    infix fun Boolean.`=`(value: Boolean) : LuaTableScope = privateSet(this, value)
    infix fun Boolean.`=`(value: Number) : LuaTableScope = privateSet(this, value)
    infix fun Boolean.`=`(value: String) : LuaTableScope = privateSet(this, value)
    infix fun Boolean.`=`(value: Map<*, *>) : LuaTableScope = privateSet(this, value)
    infix fun Boolean.`=`(value: Iterable<*>) : LuaTableScope = privateSet(this, value)
    infix fun Boolean.`=`(value: LuaTableScope.() -> Unit): LuaTableScope = privateSet(this, value)
    infix fun Number.`=`(value: Date) : LuaTableScope = privateSet(this, value)
    infix fun Number.`=`(value: Boolean) : LuaTableScope = privateSet(this, value)
    infix fun Number.`=`(value: Number) : LuaTableScope = privateSet(this, value)
    infix fun Number.`=`(value: String) : LuaTableScope = privateSet(this, value)
    infix fun Number.`=`(value: Map<*, *>) : LuaTableScope = privateSet(this, value)
    infix fun Number.`=`(value: Iterable<*>) : LuaTableScope = privateSet(this, value)
    infix fun Number.`=`(value: LuaTableBuilder): LuaTableScope = privateSet(this, value)

    override fun String.`=`(value: Date) : LuaTableScope = privateSet(this, value)
    override fun String.`=`(value: Boolean) : LuaTableScope = privateSet(this, value)
    override fun String.`=`(value: Number) : LuaTableScope = privateSet(this, value)
    override fun String.`=`(value: String) : LuaTableScope = privateSet(this, value)
    override fun String.`=`(value: Map<*, *>) : LuaTableScope = privateSet(this, value)
    override fun String.`=`(value: Iterable<*>) : LuaTableScope = privateSet(this, value)
    override fun String.`=`(value: LuaTableBuilder): LuaTableScope = privateSet(this, value)
}

private class LuaTableScopeImpl: LuaTableScope {
    override val scope: LuaTableScope = this
    val map: MutableMap<Any, Any> = mutableMapOf()

    override fun String.unaryPlus(): LuaScope {
        for (i in 1..200) {
            if (map[i] == null) {
                map[i] = this
                break
            }
        }
        return this@LuaTableScopeImpl
    }

    override fun toLua(): String = toLuaValueString(map)
}

private fun privateKeyString(key: Any): String = when (key) {
    is String -> if (key.isEmpty()) "" else "$key = "
    else -> "$key = "
}

private inline fun <reified K: Any, reified S: LuaScope> S.privateSet(key: K, value: Any): S {
    val luaKey = privateKeyString(key)


    return when (this) {
        is LuaScopeImpl -> {
            if (K::class != String::class) throw IllegalArgumentException("Key must be a String in a global scope")
            code.append("$luaKey${toLuaValueString(value)}\n")
            this
        }
        
        is LuaTableScopeImpl -> {
            map[key] = value
            this
        }

        else -> throw IllegalArgumentException("Cannot set value to ${S::class}")
    }
}

private inline fun <reified K: Any, reified S: LuaScope> S.privateSet(key: K, value: LuaTableScope.() -> Unit): S {
    val tableScope = LuaTableScopeImpl()
    tableScope.value()
    return privateSet(key, tableScope.map)
}

private class LuaScopeImpl : LuaGlobalScope {
    override val scope: LuaGlobalScope = this
    val code: StringBuilder = StringBuilder()
    override fun String.unaryPlus(): LuaGlobalScope {
        code.append("$this\n")
        return this@LuaScopeImpl
    }
    override fun toLua(): String = code.toString()
}

@LuaMarker
fun lua(value: LuaGlobalScope.() -> Unit): String {
    val scope = LuaScopeImpl()
    scope.value()
    return scope.toLua()
}

@LuaMarker
fun table(value: LuaTableScope.() -> Unit): Map<Any, Any> {
    val scope = LuaTableScopeImpl()
    scope.value()
    return scope.map
}

