package org.tribot.wikiscraper.lua

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/* Written by IvanEOD 12/10/2022, at 6:27 PM */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@DslMarker
annotation class LuaMarker

typealias LuaTableBuilder = LuaTableScope.() -> Unit

private val LuaDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
private fun toLuaKeyString(key: Any): String = "[${toLuaValueString(key)}]"
private fun toLuaValueString(value: Any): String = when (value) {
    is String -> "'$value'"
    is Date -> "'${LuaDateFormat.format(value)}'"
    is Boolean,
    is Number -> value.toString()
    is Map<*, *> -> value.entries.joinToString(",", "{", "}") {
            (k, v) -> "${toLuaKeyString(k!!)}=${toLuaValueString(v!!)}"
    }
    is List<*> -> value.joinToString(",", "{", "}") { toLuaValueString(it!!) }
    is LocalModifier<*> -> toLuaValueString(value.value)
    else -> throw IllegalArgumentException("Cannot convert $value to Lua")
}

@DslMarker
annotation class LocalScopeMarker

@LocalScopeMarker
interface LocalScope {

    fun String.local() = LocalModifier.new(this)
    fun Date.local() = LocalModifier.new(this)
    fun Boolean.local() = LocalModifier.new(this)
    fun Number.local() = LocalModifier.new(this)
    fun Map<*, *>.local() = LocalModifier.new(this)
    fun Iterable<*>.local() = LocalModifier.new(this)
    fun LuaTableBuilder.local() = LocalModifier.new(this)

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
    infix fun String.`=`(value: LuaTableScope.() -> Unit): LuaScope = privateSet(this, value)

    operator fun String.unaryPlus(): LuaScope

    fun toLua(): String


}

interface LuaGlobalScope: LuaScope, LocalScope {
    override val scope: LuaGlobalScope
        get() = this

    infix fun <T: Any> String.`=`(value: LocalModifier<T>): LuaScope = privateSet(this, value.value, true)

    operator fun File.unaryPlus(): LuaGlobalScope {
        if (!exists()) throw IllegalArgumentException("File $this does not exist")
        val code = readText()
        +code
        return this@LuaGlobalScope
    }

    fun require(module: String): LuaGlobalScope {
        if (module.isEmpty()) return this
        var name = module
        if (name.startsWith("Module:")) name = name.substring(7).ifEmpty { return this }
        name = name.replaceFirstChar { it.lowercase() }.split(" ").joinToString { "" }
        return require(name, module)
    }

    fun require(variableName: String, module: String): LuaGlobalScope {
        val moduleName = if (module.startsWith("Module:")) module else "Module:$module"
        //

        return this
    }


}


class LocalModifier<T: Any> private constructor(val value: T) {
    companion object {
        internal fun <T: Any> new(value: T) = LocalModifier(value)
    }
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

private inline fun <reified K: Any, reified S: LuaScope> S.privateSet(key: K, value: Any, local: Boolean = false): S {
    return when (this) {
        is LuaScopeImpl -> {
            code.append("${if (local) "local " else ""}$key = ${toLuaValueString(value)}\n")
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



fun main() {

    val lua = lua {

        "test" `=` "test"
        "test2" `=` 1
        "test3" `=` true.local()
        "test4" `=` Date()
        "test5" `=` listOf(1, 2, 3)
        "test6" `=` mapOf("test" to 1, "test2" to 2)
        "test7" `=` table {
            "test" `=` "test"
            "test8" `=` "test9"
            "test10" `=` 1
            "test11" `=` true
            "test12" `=` Date()
            "test13" `=` listOf(1, 2, 3)
            "test14" `=` mapOf("test15" to 1, "test16" to 2)
        }
    }

    println(lua)

}

