package org.tribot.wikiscraper.lua

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.tribot.wikiscraper.OsrsWiki
import org.tribot.wikiscraper.classes.ItemDetails
import org.tribot.wikiscraper.utility.*
import org.tribot.wikiscraper.utility.getNestedJsonObject
import org.tribot.wikiscraper.utility.getString
import org.tribot.wikiscraper.utility.htmlUnescape
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

/* Written by IvanEOD 12/10/2022, at 3:55 PM */
class ScribuntoSession private constructor(private val wiki: OsrsWiki) {

    private var sessionId: Int = -1
    private var sessionSize: Int = -1
    private var sessionMaxSize: Int = -1
    private var inBuilderAddedCode = ""
    private var inSessionAddedCode = ""
    private var lastSessionCommunication: Long = 0L
    private var checkSessionAfter: Long = 5.msMinutes

    private fun addToSession(code: String) {
        inSessionAddedCode += "\n\n$code\n\n"
    }

    init {
        loadSession()

    }

    private fun loadMainLua(): String {
        return File("src/main/kotlin/org/tribot/wikiscraper/lua/Scribunto.lua").readText()
//        return File("C:\\Users\\ivanc\\OneDrive\\Desktop\\osrs-wiki-scraper\\src\\main\\kotlin\\org\\tribot\\wikiscraper\\Scribunto.lua").readText()
    }

    @Throws(ScribuntoError::class)
    private fun processResponse(response: String): ScribuntoRequestResult {
        lastSessionCommunication = System.currentTimeMillis()
        val responseJson = JsonParser.parseString(response.htmlUnescape()).asJsonObject
        val error = responseJson.has("error")
        if (error) {
            val errorMessage = responseJson.getNestedJsonObject("error")?.getString("info") ?: "Unknown error"
            throw ScribuntoRequestError(sessionId, errorMessage)
        }
        val result = ScribuntoRequestResult.fromJsonObject(responseJson)

//        result.throwIfError()
        sessionId = result.session
        sessionSize = result.sessionSize
        sessionMaxSize = result.sessionMaxSize
        return result
    }

    @Throws(ScribuntoError::class)
    private fun loadSession() {
        val module = loadMainLua()
        val parameters = mutableMapOf<String, String>()
        parameters["title"] = "Var"
        parameters["clear"] = "1"
        parameters["question"] = module + inBuilderAddedCode + inSessionAddedCode
        val response = wiki.formGet("scribunto-console", parameters)?.body?.string() ?: ""
        val result = processResponse(response)
        if (sessionSize / sessionMaxSize.toDouble() > 0.95) throw ScribuntoSessionSizeTooLarge(sessionId)

    }

    fun reloadSession(clearAddedSessionCode: Boolean = false) {
        if (clearAddedSessionCode) inSessionAddedCode = ""
        loadSession()
    }

    @Throws(ScribuntoError::class)
    private fun checkSession(): Boolean {
        if (sessionId == -1) return false
        val (success, response) = request(false, "isSessionLoaded()")
        if (!success || response.isJsonNull) return false
        return runCatching { response.asBoolean }.getOrDefault(false)
    }

    private fun JsonElement.isSuccessResponse(): Boolean = if (isJsonNull) false
        else runCatching { asBoolean }.getOrDefault(false)

    @Throws(ScribuntoError::class)
    fun loadToSession(block: LuaGlobalScope.() -> Unit) {
        val code = lua(block)
        addToSession(code)
        val response = request(false, code)
        if (!response.first || !response.second.isSuccessResponse()) throw ScribuntoGeneralError(
            sessionId,
            "Error loading code to session: $response"
        )
    }

    fun request(resetSession: Boolean = false, block: LuaGlobalScope.() -> Unit): Pair<Boolean, JsonElement> =
        request(resetSession, lua(block))

    fun request(resetSession: Boolean = false, code: String, attempt: Int = 1): Pair<Boolean, JsonElement> {
        val parameters = mutableMapOf<String, String>()
        parameters["title"] = "Var"

        if (!resetSession && sessionId != -1) {
            if (System.currentTimeMillis() - lastSessionCommunication > checkSessionAfter) {
                if (!checkSession()) loadSession()
            }
            parameters["session"] = sessionId.toString()
        } else loadSession()

        parameters["question"] = code
        val response = wiki.basicGet("scribunto-console", parameters)?.body?.string() ?: ""
        val result = processResponse(response)
        if (result.isError()) {
            if (attempt > 3) result.throwIfError()
            else {
                println("Error in request, retrying...")
//                reloadSession()
                return request(true, code, attempt + 1)
            }
        }

        return result.isError() to (result.print?.get("printReturn") ?: JsonNull.INSTANCE)
    }

    private data class ScribuntoRequestResult(
        val type: String,
        val print: JsonObject?,
        val returnObject: JsonObject?,
        val html: String,
        val message: String,
        val messageName: String,
        val session: Int,
        val sessionSize: Int,
        val sessionMaxSize: Int
    ) {
        fun isError(): Boolean {
            if (type == "error") return true
            return false
        }

        @Throws(ScribuntoError::class)
        fun throwIfError() {
            if (isError()) {
                if (messageName == "scribunto-lua-error-location") throw ScribuntoLuaError(session, message)
                else throw ScribuntoGeneralError(session, message)
            }
        }

        override fun toString(): String =
            "RequestResult(type='$type', print=$print, returnObject=$returnObject, html='$html', message='$message', messageName='$messageName', session=$session, sessionSize=$sessionSize, sessionMaxSize=$sessionMaxSize)"

        companion object {

            fun fromJsonObject(json: JsonObject): ScribuntoRequestResult {
                val responseType = json.getString("type")
                val session = json.getString("session").toIntOrNull() ?: -1
                val sessionSize = json.getString("sessionSize").toIntOrNull() ?: -1
                val sessionMaxSize = json.getString("sessionMaxSize").toIntOrNull() ?: -1

                if (responseType == "error") {
                    val errorMessage = json.getString("message").ifEmpty { "Unknown error" }
                    val errorName = json.getString("messagename").ifEmpty { "Unknown" }
                    val html = json.getString("html")
                    return ScribuntoRequestResult(
                        "error", null, null,
                        html, errorMessage, errorName,
                        session, sessionSize, sessionMaxSize
                    )
                }

                val print = json.getString("print").htmlUnescape()
                val returnString = json.getString("return").htmlUnescape()
                val printObject = runCatching { JsonParser.parseString(print).asJsonObject }.getOrNull()
                val returnObject = runCatching { JsonParser.parseString(returnString).asJsonObject }.getOrNull()

                return ScribuntoRequestResult(
                    json.getString("type"),
                    printObject, returnObject,
                    json.getString("html").htmlUnescape(),
                    json.getString("message").htmlUnescape(),
                    json.getString("messagename").htmlUnescape(),
                    json.getString("session").toIntOrNull() ?: -1,
                    json.getString("sessionSize").toIntOrNull() ?: -1,
                    json.getString("sessionMaxSize").toIntOrNull() ?: -1
                )
            }

        }
    }

    inner class Builder internal constructor() {

        fun checkSessionAfter(time: Long, unit: TimeUnit): Builder {
            checkSessionAfter = unit.toMillis(time)
            return this
        }

        fun lua(code: String): Builder {
            inBuilderAddedCode += "\n\n$code\n\n"
            return this
        }

        fun lua(file: File): Builder = lua(file.readText())

        fun build(): ScribuntoSession = this@ScribuntoSession

    }

    companion object {
        @JvmStatic
        fun build(wiki: OsrsWiki, init: Builder.() -> Unit): ScribuntoSession {
            val builder = ScribuntoSession(wiki).Builder()
            builder.init()
            return builder.build()
        }
    }

}


fun main() {
    val wiki = OsrsWiki.builder().build()
    val session = wiki.scribuntu {
        checkSessionAfter(5, TimeUnit.MINUTES)

    }

    val title = "Baby chinchompa"
//    session.getTemplatesOnPage(title)

//    println(session.getPagesInCategory("Items", "Pets"))

//    val results = session.getAllExchangeData()
//    runBlocking {
//        val results: List<ItemDetails>
//        val time = measureTimeMillis {
//            results = session.getItemDetails("Black chinchompa")
//        }
//        results.forEach {
//            it.debug()
//        }
//
//        println("Request completed in $time ms")
//    }
//    val results = session.getTitlesWithLocationData()
//        println(results)
//
//    println(results.size)
//

    val results = session.getAllItemDetails()
    println(results.size)

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






    println("Loading main lua")


}