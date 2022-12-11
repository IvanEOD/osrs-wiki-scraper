package org.tribot.wikiscraper.lua

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.tribot.wikiscraper.OsrsWiki
import org.tribot.wikiscraper.utility.getNestedJsonObject
import org.tribot.wikiscraper.utility.getString
import org.tribot.wikiscraper.utility.htmlUnescape
import org.tribot.wikiscraper.utility.msMinutes
import java.io.File
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

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

    private fun loadMainLua(): String {
        return File("C:\\Users\\ivanc\\OneDrive\\Desktop\\osrs-wiki-scraper\\src\\main\\kotlin\\org\\tribot\\wikiscraper\\Scribunto.lua").readText()
    }

    @Throws(ScribuntoError::class)
    private fun processResponse(response: String): ScribuntoRequestResult {
        lastSessionCommunication = System.currentTimeMillis()
        val responseJson = JsonParser.parseString(response.htmlUnescape()).asJsonObject
        val error = responseJson.has("error")
        if (error) {
            val errorMessage = responseJson.getNestedJsonObject("error")?.getString("info") ?: "Unknown error"
            throw ScribuntoRequestError(errorMessage)
        }
        val result = ScribuntoRequestResult.fromJsonObject(responseJson)
        result.throwIfError()
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
        val response = wiki.basicGet("scribunto-console", parameters)?.body?.string() ?: ""
        val result = processResponse(response)
        if (sessionSize / sessionMaxSize.toDouble() > 0.95) throw ScribuntoSessionSizeTooLarge

    }

    fun reloadSession(clearAddedSessionCode: Boolean = false) {
        if (clearAddedSessionCode) inSessionAddedCode = ""
        loadSession()
    }

    @Throws(ScribuntoError::class)
    private fun checkSession() : Boolean {
        if (sessionId == -1) return false
        val response = request("printReturn(isSessionLoaded())")
        if (response.isJsonNull) return false
        return runCatching { response.asBoolean }.getOrDefault(false)
    }

    private fun JsonElement.isSuccessResponse(): Boolean = if (isJsonNull) false
        else runCatching { asBoolean }.getOrDefault(false)

    @Throws(ScribuntoError::class)
    fun loadToSession(code: String) {
        addToSession(code)
        val response = request(code)
        if (!response.isSuccessResponse()) throw ScribuntoGeneralError("Error loading code to session: $response")
    }


    fun request(code: String): JsonElement {

        val parameters = mutableMapOf<String, String>()
        parameters["title"] = "Var"
        if (sessionId != -1) {
            if (System.currentTimeMillis() - lastSessionCommunication > checkSessionAfter) {
                if (!checkSession()) loadSession()
            }
            parameters["session"] = sessionId.toString()
        }
        parameters["question"] = code
        val response = wiki.basicGet("scribunto-console", parameters)?.body?.string() ?: ""
        val result = processResponse(response)
        return result.print?.get("printReturn") ?: JsonNull.INSTANCE
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
        fun isError(): Boolean = type == "error"
        fun isLuaError(): Boolean = messageName.isNotEmpty() && messageName.startsWith("scribunto-lua-error")

        val hasPrints get() = print != null
        val hasHtml get() = html.isNotEmpty()
        val hasReturns get() = returnObject != null
        val hasMessage get() = message.isNotEmpty()

        @Throws(ScribuntoError::class)
        fun throwIfError() {
            if (isError()) {
                if (messageName == "scribunto-lua-error-location") throw ScribuntoLuaError(message)
                else throw ScribuntoGeneralError(message)
            }
        }

        override fun toString(): String {
            return "RequestResult(type='$type', print=$print, returnObject=$returnObject, html='$html', message='$message', messageName='$messageName', session=$session, sessionSize=$sessionSize, sessionMaxSize=$sessionMaxSize)"
        }

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

    session.request("printReturn(isSessionLoaded())")

    println("Loading main lua")


}