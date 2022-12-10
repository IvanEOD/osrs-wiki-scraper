package org.tribot.wikiscraper.query

import org.tribot.wikiscraper.OsrsWiki
import org.tribot.wikiscraper.utility.getNestedJsonObject
import org.tribot.wikiscraper.utility.getString
import org.tribot.wikiscraper.utility.htmlUnescape
import org.tribot.wikiscraper.utility.pipeFence

/* Written by IvanEOD 12/9/2022, at 8:19 AM */
class AskRequest private constructor(private val wiki: OsrsWiki) {
    var requestLimit: Int = 5000
    var responseLimit: Int = Int.MAX_VALUE
    private var offset: Int = 0
    var conditions: MutableList<String> = mutableListOf()
    var printouts: MutableList<String> = mutableListOf()
    var parameters: MutableMap<String, String> = mutableMapOf()

    private fun formatQuery(offset: Int, limit: Int = requestLimit): String {
        return buildString {
            conditions.forEach {
                if (it.isNotBlank()) {
                    if (!it.startsWith("[[")) append("[[")
                    append(it)
                    if (!it.endsWith("]]")) append("]]")
                }
            }
            append("\n")
            if (printouts.isNotEmpty()) append("|${printouts.pipeFence("?", "\n")}")
            val params = parameters.toMutableMap()
            val maxLimit = responseLimit - offset
            params["limit"] = minOf(limit, maxLimit).toString()
            params["offset"] = offset.toString()
            params["format"] = "json"
            append("|${params.entries.joinToString("|") { "${it.key}=${it.value}\n" }}")
        }
    }

    fun getResults(offset: Int): List<AskResult> {
        val query = formatQuery(offset)
        return ask(query)
    }

    fun getResults(): List<AskResult> {
        var receivedAll = false
        val combinedResults = mutableListOf<AskResult>()
        offset = 0
        while (offset < responseLimit && !receivedAll) {
            val results = getResults(offset)
            if (results.isEmpty()) break
            receivedAll = combinedResults.size < requestLimit
            offset += results.size
            combinedResults.addAll(results)
        }
        return combinedResults
    }

    private fun ask(queryString: String): List<AskResult> {
        val result = mutableListOf<AskResult>()
        val query = wiki.newQuery(WikiQuery.Ask)
        query["query"] = queryString
        while (query.isNotEmpty()) {
            val queryResult = query.next()!!
            val results = queryResult.input.getNestedJsonObject("query", "results")?.entrySet() ?: emptySet()
            for ((title, element) in results) {

                val printouts = mutableMapOf<String, String>()
                val elementObject = element.asJsonObject

                val fulltext = elementObject.getString("fulltext")
                val fullurl = elementObject.getString("fullurl")
                val namespace = elementObject.getString("namespace")
                val exists = elementObject.getString("exists")

                val properties = elementObject.get("printouts").asJsonObject
                for ((key, value) in properties.entrySet()) {
                    val valueArray = value.asJsonArray
                    if (valueArray.isEmpty) {
                        printouts[key] = ""
                        continue
                    }
                    val jsonElement = valueArray[0]!!
                    var stringValue = ""
                    stringValue = if (key == "image") jsonElement.asJsonObject.get("fullurl").asString
                    else if (jsonElement.isJsonPrimitive) jsonElement.asString
                    else if (jsonElement.isJsonObject) jsonElement.asJsonObject.get("fulltext").asString
                    else if (jsonElement.isJsonArray) jsonElement.asJsonArray.joinToString(", ") { it.asString }
                    else throw IllegalStateException("Unknown json element type: ${jsonElement.javaClass}")
                    printouts[key] = stringValue.htmlUnescape()
                }
                val askResult = AskResult(title, printouts, fulltext, fullurl, namespace.toIntOrNull() ?: 0, exists == "1")
                result.add(askResult)
            }
        }
        return result
    }

    inner class Builder internal constructor() {
        fun requestLimit(limit: Int) = apply {
            this@AskRequest.requestLimit = limit
        }

        fun responseLimit(limit: Int) = apply {
            this@AskRequest.responseLimit = limit
        }

        fun conditions(vararg conditions: String) = apply {
            this@AskRequest.conditions.addAll(conditions)
        }

        fun condition(condition: String) = conditions(condition)

        fun printouts(vararg printouts: String) = apply {
            this@AskRequest.printouts.addAll(printouts)
        }

        fun printout(printout: String) = printouts(printout)

        fun parameters(parameters: Map<String, String>) = apply {
            this@AskRequest.parameters.putAll(parameters)
        }

        fun parameter(key: String, value: String) = apply {
            this@AskRequest.parameters[key] = value
        }

        fun build() = this@AskRequest
    }

    companion object {
        fun builder(wiki: OsrsWiki) = AskRequest(wiki).Builder()
    }

}