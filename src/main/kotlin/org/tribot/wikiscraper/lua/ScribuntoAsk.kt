package org.tribot.wikiscraper.lua

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.tribot.wikiscraper.classes.ItemDetails
import org.tribot.wikiscraper.classes.ItemDetails.Companion.boolean
import org.tribot.wikiscraper.classes.Coordinates
import org.tribot.wikiscraper.classes.LocationDetails
import org.tribot.wikiscraper.utility.*
import org.tribot.wikiscraper.utility.GSON
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque


/* Written by IvanEOD 12/12/2022, at 2:26 PM */

object WikiModules {
    const val GEPriceData = "Module:GEPrices/data"
    const val GEVolumesData = "Module:GEVolumes/data"
    const val LastPricesData = "Module:LastPrices/data"
}


fun ScribuntoSession.dplAsk(query: Map<String, Any>): JsonElement {
    val (success, response) = request {
        "query" `=` query.local()
        +"dplAsk(query, true)"
    }
    return response
}

fun ScribuntoSession.smwAsk(query: List<String>): JsonElement {
    val (success, response) = request {
        "query" `=` query.local()
        +"smwAsk(query, true)"
    }
    return response
}

fun ScribuntoSession.getTemplatesOnPage(title: String): List<String> {
    val (success, response) = request {
        +"getTemplatesOnPage(\"$title\", true)"
    }
    return responseToArray(response).map { it.asString }
}


fun ScribuntoSession.getPagesInCategory(vararg categories: String): List<String> {
    val list = mutableListOf<String>()
    val chunkSize = 10000
    for (category in categories) {
        var offset = 0
        while (true) {
            val (success, response) = request {
                +"getPagesInCategory(\"$category\", $chunkSize, $offset, true)"
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
    val (success, response) = request {
        +"loadExchangeData({\"$itemName\"}, true)"
    }
    return GSON.fromJson(response, WikiExchangeData::class.java)
}

fun ScribuntoSession.getExchangeData(itemNames: List<String>): Map<String, WikiExchangeData> {
    val map = mutableMapOf<String, WikiExchangeData>()
    val chunkSize = 200
    val queue = TitleQueue(itemNames, chunkSize)
    val main = GlobalScope.launch {
        with (queue) {
            process { list ->
                val (success, response) = request {
                    "data" `=` list.local()
                    +"loadExchangeData(data, true, true)"
                }
                if (success) {
                    val data: Map<String, WikiExchangeData> = GSON.fromJson(response, WikiStringExchangeDataMapType)
                    map.putAll(data)
                    emptyList()
                } else list
            }
        }
    }
    runBlocking { main.join() }
    return map
}

fun ScribuntoSession.getExchangeData(vararg itemNames: String): Map<String, WikiExchangeData> =
    getExchangeData(itemNames.toList())

fun ScribuntoSession.getAllExchangeData(): Map<String, WikiExchangeData> {
    val titles = getAllItemTitles()
    return getExchangeData(titles)
}

fun ScribuntoSession.getItemDetails(itemName: String): List<ItemDetails> {
    val (success, result) = request {
        +"loadItemData({\"$itemName\"}, true)"
    }
    val itemResult = result.asJsonObject[itemName]
    return buildItemDetails(itemResult).second
}

fun ScribuntoSession.getItemDetails(itemNames: List<String>): Map<String, List<ItemDetails>> {
    val map = mutableMapOf<String, MutableList<ItemDetails>>()
    val chunkSize = 100
    val queue = TitleQueue(itemNames, chunkSize)
    val main = GlobalScope.launch {
        with (queue) {
            process { list ->
                val (success, result) = request {
                    "sessionData" `=` list
                    +"loadItemData(sessionData, true)"
                }
                if (!success) list
                else {
                    val data = result.asJsonObject
                    for (title in list) {
                        val result = data.getAsJsonObject(title)
                        val details = buildItemDetails(result)
                        if (details.first.isNotEmpty() && details.second.isNotEmpty()) {
                            val list = map.getOrPut(details.first) { mutableListOf() }
                            list.addAll(details.second)
                        }
                    }
                    emptyList()
                }
            }
        }
    }
    runBlocking { main.join() }
    return map
}

fun ScribuntoSession.getItemDetails(vararg itemNames: String): Map<String, List<ItemDetails>> =
    getItemDetails(itemNames.toList())

fun ScribuntoSession.getAllItemDetails(): Map<String, List<ItemDetails>> {
    val titles = getAllItemTitles()
    return getItemDetails(titles)
}


fun ScribuntoSession.getTitlesWithLocationData(): List<String> {
    val returnList = mutableListOf<String>()

    runChunks(500) { chunkSize, offset ->
        val (success, request) = request {
            +"loadTitlesWithLocationData($chunkSize, $offset, true)"
        }
        val array = request.asJsonArray.map { cleanLocationName(it.asJsonArray[0].asString) }
        val returnSize = array.size
        returnList.addAll(array)
        returnList.distinct()
        returnSize
    }
    return returnList
}

fun ScribuntoSession.getLocationJson(title: String): LocationDetails? {
    val (success, request) = request {
        +"loadLocationDataByTitle(\"$title\", true)"
    }
    val obj = if (request.isJsonArray) {
        val array = request.asJsonArray
        if (array.isEmpty) return null
        else array[0].asJsonObject
    } else request.asJsonObject
    val locationJson = JsonParser.parseString(obj["Location JSON"].asString)
    return buildLocationDetails(locationJson)
}

fun ScribuntoSession.getLocationJson(): Map<String, List<LocationDetails>> {
    val results = mutableMapOf<String, MutableList<LocationDetails>>()
    runChunks(500) { chunkSize, offset ->
        val (success, response) = request {
            +"loadLocationData($chunkSize, $offset, true)"
        }
        val responseArray = responseToArray(response)
        val responseSize = responseArray.size()
        fun addLocation(name: String, location: LocationDetails) {
            val list = results.getOrPut(name) { mutableListOf() }
            list.add(location)
        }

        responseArray.forEach { element ->
            val name = runCatching {
                element.asJsonObject["1"].asString
            }.onFailure {
                println("Failed to get name for (${element.javaClass.simpleName}) $element")
            }.getOrDefault("Unknown Name")
            val cleanedName = cleanLocationName(name)
            val json = element.asJsonObject["Location JSON"]
            if (json.isJsonArray) {
                val array = json.asJsonArray
                array.forEach {
                    val details = buildLocationDetails(JsonParser.parseString(it.asString))
                    addLocation(cleanedName, details)
                }
            } else {
                val details = buildLocationDetails(JsonParser.parseString(json.asString))
                addLocation(cleanedName, details)
            }
        }
        responseSize
    }
    return results
}

//function loadTitlesWithLocationData(limit, offset, printResults)
//    local results = mw.smw.ask {
//        "[[Location JSON::+]]",
//        "limit = " .. limit,
//        "offset = " .. offset,
//    }
//    if (printResults) then
//        printReturn(results)
//    end
//    return results
//end

fun ScribuntoSession.getQuestDetails() {
    TODO()
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
) {

    fun debug(prefix: String) {
        fun prefixPrint(string: String) = println("$prefix$string")
        prefixPrint("Exchange Data:")
        prefixPrint("    Exchange Name: $name")
        prefixPrint("    Exchange id: $id")
        prefixPrint("    Data Graph Link: $link")
        prefixPrint("    Price: $price")
        prefixPrint("    Last price: $last")
        prefixPrint("    Price change: $change")
        prefixPrint("    Price date: $date")
        prefixPrint("    Last price date: $lastDate")
        prefixPrint("    Buy limit: $buyLimit")
        prefixPrint("    Exchange volume: $volume")
        prefixPrint("    Members: $members")
        prefixPrint("    Icon: $icon")
        prefixPrint("    Examine: $examine")
        prefixPrint("    Value: $value")
        prefixPrint("    High alch: $highAlch")
        prefixPrint("    Low alch: $lowAlch")


    }


}

fun runChunks(chunkSize: Int, worker: (Int, Int) -> Int) {
    var offset = 0
    while (true) {
        val processedAmount = worker(chunkSize, offset)
        offset += processedAmount
        if (processedAmount < chunkSize) break
    }
}


private fun buildItemDetails(jsonElement: JsonElement): Pair<String, List<ItemDetails>> {
    val list = mutableListOf<ItemDetails>()
    val resultObject = jsonElement.asJsonObject
    val infoObject = resultObject["info"]
    val info = if (infoObject.isJsonObject) resultObject["info"].asJsonObject else return "" to emptyList()
    val bonusResult = resultObject["bonuses"]
    val bonuses = if (bonusResult != null) {
        if (bonusResult.isJsonArray) {
            val array = bonusResult.asJsonArray
            if (array.isEmpty) JsonNull.INSTANCE
            else array[0].asJsonObject
        } else if (bonusResult.isJsonObject) bonusResult.asJsonObject
        else JsonNull.INSTANCE
    } else JsonNull.INSTANCE
    val title = resultObject["title"].asString
    val infoboxVersions = info.toVersionedMap()
    val bonusesVersions = if (!bonuses.isJsonNull) bonuses.asJsonObject.toVersionedMap() else null
    val exchangeInfoJson =
        resultObject["exchangeData"]?.let { if (it.isJsonObject) it.asJsonObject else JsonNull.INSTANCE }
            ?: JsonNull.INSTANCE
    val exchangeData = if (exchangeInfoJson.isJsonNull) "" else exchangeInfoJson.toString()
    val versionCount = infoboxVersions.versions
    for (i in 0 until versionCount) {
        val infobox = infoboxVersions.getVersion(i + 1).toMutableMap()
        if ((infobox["exchange"]?.boolean() ?: infobox["tradeable"]?.boolean()) == true) infobox["exchangeInfo"] =
            exchangeData
        val bonus = bonusesVersions?.getVersion(i + 1) ?: emptyMap()
        val details = ItemDetails.fromMap(infobox, bonus)
        list.add(details)
    }
    return title to list
}

private fun buildLocationDetails(element: JsonElement): LocationDetails {
    val obj = element.asJsonObject
    val locationType = obj["type"].asString
    val geometryObject = obj["geometry"].asJsonObject
    val geometryType = geometryObject["type"].asString
    val coordinates = geometryObject["coordinates"].asJsonArray
    val properties = obj["properties"].asJsonObject
    val mapId = properties["mapID"].asInt
    val plane = properties["plane"].asInt
    val locationCoordinatesList = mutableListOf<Coordinates>()

    for (coordinate in coordinates) {
        val coordinateList = GSON.fromJson(coordinate, Array<Array<Int>>::class.java)
        for (coordinatePair in coordinateList) {
            val x = coordinatePair[0]
            val y = coordinatePair[1]
            val location = Coordinates(x, y, plane)
            locationCoordinatesList.add(location)
        }
    }

    return LocationDetails(locationType, mapId, geometryType, locationCoordinatesList)
}

private fun cleanLocationName(name: String): String {
    return name.replace("[[", "").replace("]]", "")
        .split("|").last()
}

private fun responseToArray(response: JsonElement): JsonArray {
    if (response is JsonArray) return response
    val responseObject = response.asJsonObject
    responseObject.remove("DPL time")
    responseObject.remove("Parse time")
    val array = JsonArray()
    responseObject.entrySet().forEach {
        array.add(it.value)
    }
    return array
}

private val WikiStringExchangeDataMapType = object : TypeToken<Map<String, WikiExchangeData>>() {}.type
private val ignorePrefixes = listOf("Template:", "Module:", "Category:", ":Category:", "File:")