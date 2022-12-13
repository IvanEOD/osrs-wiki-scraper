package org.tribot.wikiscraper.lua

import com.google.gson.JsonArray
import com.google.gson.JsonElement


/* Written by IvanEOD 12/12/2022, at 2:26 PM */

fun ScribuntoSession.ask(query: Map<String, Any>): JsonElement {
    val response = request {
        "query" `=` query.local()
        +"dplAsk(query)"
    }
    return response
}

fun ScribuntoSession.getTemplatesOnPage(title: String): List<String> {
    val response = request {
        +"getTemplatesOnPage('$title')"
    }
    return responseToArray(response).map { it.asString }
}

private val ignorePrefixes = listOf("Template:", "Module:", "Category:", ":Category:", "File:")

fun ScribuntoSession.getPagesInCategory(vararg categories: String): List<String> {
    val list = mutableListOf<String>()
    val chunkSize = 1000
    for (category in categories) {
        var offset = 0
        while (true) {
            val response = request {
                +"getPagesInCategory('$category', $chunkSize, $offset)"
            }
            val titles = responseToArray(response)
            titles.forEach { list.add(it.asString) }
            if (titles.size() < chunkSize) break
            else offset += chunkSize
        }
    }
    return list.filter { title -> ignorePrefixes.none { title.startsWith(it) } }.distinct()
}

private fun responseToArray(response: JsonElement): JsonArray {
    val responseObject = response.asJsonObject
    responseObject.remove("DPL time")
    responseObject.remove("Parse time")
    val array = JsonArray()
    responseObject.entrySet().forEach {
        array.add(it.value)
    }
    return array
}