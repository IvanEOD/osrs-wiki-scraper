package org.tribot.wikiscraper.lua

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.tribot.wikiscraper.utility.getString
import org.tribot.wikiscraper.utility.htmlUnescape


/* Written by IvanEOD 12/18/2022, at 10:32 AM */
data class ScribuntoResponse(
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

        fun fromJsonObject(json: JsonObject): ScribuntoResponse {
            val responseType = json.getString("type")
            val session = json.getString("session").toIntOrNull() ?: -1
            val sessionSize = json.getString("sessionSize").toIntOrNull() ?: -1
            val sessionMaxSize = json.getString("sessionMaxSize").toIntOrNull() ?: -1

            if (responseType == "error") {
                val errorMessage = json.getString("message").ifEmpty { "Unknown error" }
                val errorName = json.getString("messagename").ifEmpty { "Unknown" }
                val html = json.getString("html")
                return ScribuntoResponse(
                    "error", null, null,
                    html, errorMessage, errorName,
                    session, sessionSize, sessionMaxSize
                )
            }

            val print = json.getString("print").htmlUnescape()
            val returnString = json.getString("return").htmlUnescape()
            val printObject = runCatching { JsonParser.parseString(print).asJsonObject }.getOrNull()
            val returnObject = runCatching { JsonParser.parseString(returnString).asJsonObject }.getOrNull()

            return ScribuntoResponse(
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