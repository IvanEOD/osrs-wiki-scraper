package org.tribot.wikiscraper.lua

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.tribot.wikiscraper.OsrsWiki
import org.tribot.wikiscraper.query.getAllItemDetails
import org.tribot.wikiscraper.query.getItemDetails
import org.tribot.wikiscraper.utility.getNestedJsonObject
import org.tribot.wikiscraper.utility.getString
import org.tribot.wikiscraper.utility.htmlUnescape
import kotlin.system.measureTimeMillis

/* Written by IvanEOD 12/10/2022, at 3:55 PM */
class ScribuntoSession internal constructor(private val wiki: OsrsWiki, sessionModule: String? = null, sessionCode: String? = null) {
    var successfulRequests = 0
    var failedRequests = 0

    val module: String = sessionModule ?: "Var"
    val code: String = sessionCode ?: SessionManager.loadMainLua()

    var id: Int = -1
        private set
    var maxSize: Int = 0
        private set
    var size: Int = 0
        private set

    init {
        load()
    }

    private fun parameters(module: String, code: String, clear: Boolean): MutableMap<String, String> {
        val map = mutableMapOf(
            "title" to module,
            "question" to code,
            "session" to id.toString(),
        )
        if (clear) map["clear"] = "1"
        return map
    }

    fun isSession(id: Int) = this.id == id

    fun validate(): Boolean {
        if (failedRequests > 10) return false
        val (success, response) = sendRequest("isSessionLoaded()")
        if (!success || response.isJsonNull) return false
        return runCatching { response.asBoolean }.getOrDefault(false)
    }

    @Throws(ScribuntoError::class)
    fun update(response: ScribuntoResponse) {
        if (id == -1) {
            id = response.session
            size = response.sessionSize
            maxSize = response.sessionMaxSize
        }
        if (id != response.session) throw ScribuntoSessionError(id, "Session ID mismatch (expected $id, got ${response.session})")
        if (response.isError()) failedRequests++
        else {
            successfulRequests++
            size = response.sessionSize
        }
        if (failedRequests > 10) {
            println("Session $id has failed too many requests, refreshing...")
            refresh()
        }
    }

    @Throws(ScribuntoError::class)
    fun refresh() {
        failedRequests = 0
        successfulRequests = 0
        val (success, _) = sendRequest(true, code)
        if (!success) throw ScribuntoSessionError(id, "Failed to refresh session")
    }


    @Throws(ScribuntoError::class)
    fun sendRequest(code: String) = sendRequest(false, code)

    @Throws(ScribuntoError::class)
    fun sendRequest(refreshSession: Boolean, code: String): Pair<Boolean, JsonElement> {
//        println("Code: $code")
        val parameters = parameters(module, code, refreshSession)
        val response = wiki.formGet("scribunto-console", parameters)?.body?.string() ?: ""
        return processResponse(response)
    }

    @Throws(ScribuntoError::class)
    fun sendRequest(block: LuaGlobalScope.() -> Unit) = sendRequest(false, block)

    @Throws(ScribuntoError::class)
    fun sendRequest(refreshSession: Boolean, block: LuaGlobalScope.() -> Unit): Pair<Boolean, JsonElement> =
        sendRequest(refreshSession, lua(block))

    @Throws(ScribuntoError::class)
    private fun processResponse(response: String): Pair<Boolean, JsonElement> {
        val responseJson = JsonParser.parseString(response.htmlUnescape()).asJsonObject
        val error = responseJson.has("error")
        if (error) {
            val errorMessage = responseJson.getNestedJsonObject("error")?.getString("info") ?: "Unknown error"
            throw ScribuntoRequestError(id, errorMessage)
        }
        val result = ScribuntoResponse.fromJsonObject(responseJson)
        update(result)
        return !result.isError() to (result.print?.get("printReturn") ?: JsonNull.INSTANCE)
    }

    private fun load() {
        val (success, result) = sendRequest(true, code)
        if (!success) throw ScribuntoSessionError(id, "Failed to load session")
    }

    class Builder internal constructor() {
        internal var includeDefaultLua: Boolean = true
        internal var module: String = "Var"
        internal var code: String = ""
        fun withWikiModule(module: String) = apply { this.module = module.replaceFirst("Module:", "") }
        fun withoutDefaultCode() = apply { includeDefaultLua = false }
        fun withCode(code: String) = apply { this.code = code }
        fun withCode(block: LuaGlobalScope.() -> Unit) = apply { this.code = lua(block) }
    }

}


@OptIn(DelicateCoroutinesApi::class)
fun main() {
    val wiki = OsrsWiki.builder().build()

    val job = GlobalScope.launch {
        val time = measureTimeMillis {
            val results = wiki.getItemDetails("Abyssal whip", "Baby chinchompa", "Black chinchompa")
            results.forEach { (title, templates) ->
                println(title)
                templates.forEach { it.debug() }
            }
        }
        println("Request completed in $time ms")
    }

    runBlocking { job.join() }

//    val results = session.getLocationJson("Zaros Zeitgeist")
//    println(results)
//
//    val results = session.getLocationJson()
//    for (result in results) {
//        println("${ result.key } = ${ result.value }")
//    }
////
//    println(results.entries.sumOf { it.value.size })
//

//    val results = wiki.getQuestRequirements()
//    println(results)




    println("Loading main lua")


}


