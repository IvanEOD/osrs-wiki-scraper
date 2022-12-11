package org.tribot.wikiscraper.lua

import java.text.SimpleDateFormat
import java.util.*


/* Written by IvanEOD 12/10/2022, at 6:27 PM */
@DslMarker
annotation class LuaMarker

private val LuaDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
private fun toLuaKeyString(key: Any): String = "[${toLuaValueString(key)}]"
private fun toLuaValueString(value: Any): String = when (value) {
    is String -> "'$value'"
    is Date -> "'${LuaDateFormat.format(value)}'"
    is Boolean,
    is Number -> value.toString()
    is Map<*, *> -> value.entries.joinToString(",", "{", "}") {
            (k, v) -> "${toLuaKeyString(k!!)} = ${toLuaValueString(v!!)}"
    }
    is List<*> -> value.joinToString(",", "{", "}") { toLuaValueString(it!!) }
    else -> throw IllegalArgumentException("Cannot convert $value to Lua")
}


@LuaMarker
interface LuaScope {
    val scope: LuaScope
    val code: StringBuilder

    infix fun String.set(value: Date) : LuaScope = privateSet(value)
    infix fun String.set(value: Boolean) : LuaScope = privateSet(value)
    infix fun String.set(value: Number) : LuaScope = privateSet(value)
    infix fun String.set(value: String) : LuaScope = privateSet(value)
    infix fun String.set(value: Map<*, *>) : LuaScope = privateSet(value)
    infix fun String.set(value: Iterable<*>) : LuaScope = privateSet(value)
    infix fun String.set(value: LuaTableScope.() -> Unit): LuaScope = privateSet(value)

    @LuaMarker
    fun toLua(): String = code.toString()

}

interface LuaTableScope: LuaScope {
    override val scope: LuaTableScope
    infix fun Date.set(value: Date) : LuaTableScope = privateSet(value)
    infix fun Date.set(value: Boolean) : LuaTableScope = privateSet(value)
    infix fun Date.set(value: Number) : LuaTableScope = privateSet(value)
    infix fun Date.set(value: String) : LuaTableScope = privateSet(value)
    infix fun Date.set(value: Map<*, *>) : LuaTableScope = privateSet(value)
    infix fun Date.set(value: Iterable<*>) : LuaTableScope = privateSet(value)
    infix fun Date.set(value: Any): LuaTableScope = privateSet(value)
    infix fun Date.set(value: LuaTableScope.() -> Unit): LuaTableScope = privateSet(value)
    infix fun Boolean.set(value: Date) : LuaTableScope = privateSet(value)
    infix fun Boolean.set(value: Boolean) : LuaTableScope = privateSet(value)
    infix fun Boolean.set(value: Number) : LuaTableScope = privateSet(value)
    infix fun Boolean.set(value: String) : LuaTableScope = privateSet(value)
    infix fun Boolean.set(value: Map<*, *>) : LuaTableScope = privateSet(value)
    infix fun Boolean.set(value: Iterable<*>) : LuaTableScope = privateSet(value)
    infix fun Boolean.set(value: LuaTableScope.() -> Unit): LuaTableScope = privateSet(value)
    infix fun Number.set(value: Date) : LuaTableScope = privateSet(value)
    infix fun Number.set(value: Boolean) : LuaTableScope = privateSet(value)
    infix fun Number.set(value: Number) : LuaTableScope = privateSet(value)
    infix fun Number.set(value: String) : LuaTableScope = privateSet(value)
    infix fun Number.set(value: Map<*, *>) : LuaTableScope = privateSet(value)
    infix fun Number.set(value: Iterable<*>) : LuaTableScope = privateSet(value)
    infix fun Number.set(value: LuaTableScope.() -> Unit): LuaTableScope = privateSet(value)
}

private class LuaTableScopeImpl: LuaTableScope {
    override val scope: LuaTableScope = this
    override val code: StringBuilder = StringBuilder()
    override fun toLua(): String = "{${code.toString().removeSuffix(",")}}"
}

context(LuaScope)
private inline fun <reified T: Any, reified S: LuaScope> T.privateSet(value: Any): S {
    code.append("${toLuaKeyString(this)}=${toLuaValueString(value)},")
    return scope as S
}
context(LuaScope)
private inline fun <reified T: Any, reified S: LuaScope> T.privateSet(value: LuaTableScope.() -> Unit): S {
    val tableScope = LuaTableScopeImpl()
    tableScope.value()
    code.append("${toLuaKeyString(this)}=${tableScope.toLua()},")
    return scope as S
}

private class LuaScopeImpl : LuaScope {
    override val scope: LuaScope = this
    override val code: StringBuilder = StringBuilder()
}

fun lua(value: LuaScope.() -> Unit): String {
    val scope = LuaScopeImpl()
    scope.value()
    return scope.toLua()
}

fun luaTable(value: LuaTableScope.() -> Unit): String {
    val scope = LuaTableScopeImpl()
    scope.value()
    return scope.toLua()
}


