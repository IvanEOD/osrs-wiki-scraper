package org.tribot.wikiscraper.lua

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.tribot.wikiscraper.utility.DefaultDate
import org.tribot.wikiscraper.utility.GSON
import java.util.*


/* Written by IvanEOD 12/12/2022, at 2:26 PM */

object WikiModules {
    const val GEPriceData = "Module:GEPrices/data"
    const val GEVolumesData = "Module:GEVolumes/data"
    const val LastPricesData = "Module:LastPrices/data"
}

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
    return list.filter { title -> title !in categories && ignorePrefixes.none { title.startsWith(it) } }.distinct()
}

fun ScribuntoSession.getAllItemTitles(): List<String> = getPagesInCategory("Items", "Pets")

fun ScribuntoSession.getExchangeData(itemName: String): WikiExchangeData {
    val response = request {
        +"loadExchangeData({'$itemName'})"
    }
    return GSON.fromJson(response, WikiExchangeData::class.java)
}

fun ScribuntoSession.getExchangeData(itemNames: List<String>): List<WikiExchangeData> {
    val list = mutableListOf<WikiExchangeData>()
    val chunkSize = 100
    val max = itemNames.size

    itemNames.chunked(chunkSize).forEach { chunk ->
      val response = request {
          "sessionData" `=` chunk
          +"loadExchangeData(sessionData)"
      }
        val data = GSON.fromJson(response, Array<WikiExchangeData>::class.java)
        list.addAll(data)

    }
    return list
}

fun ScribuntoSession.getExchangeData(vararg itemNames: String): List<WikiExchangeData> =
    getExchangeData(itemNames.toList())

fun ScribuntoSession.getAllExchangeData(): Map<String, WikiExchangeData> {
    val titles = getAllItemTitles()
    val data = getExchangeData(titles)

    println(data)

    return emptyMap()
}


//fun ScribuntoSession.getVarbitTitles(): List<String> {
//
//}


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


data class WikiExchangeData(
    var name: String = "",
    var link: String = "",
    var icon: String = "",
    var examine: String = "",
    var volume: Int = 0,
    var value: Int = 0,
    var price: Int = 0,
    var lowAlch: Int = 0,
    var last: Int = 0,
    var id: Int = 0,
    var highAlch: Int = 0,
    var buyLimit: Int = 0,
    var change: Double = 0.0,
    var lastDate: Date = DefaultDate,
    var date: Date = DefaultDate,
    var members: Boolean = false,
)