package org.tribot.wikiscraper.query

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import org.tribot.wikiscraper.OsrsWiki
import org.tribot.wikiscraper.classes.*
import org.tribot.wikiscraper.lua.TitleQueue
import org.tribot.wikiscraper.utility.*
import java.util.Date


/* Written by IvanEOD 12/9/2022, at 8:36 AM */
object WikiModules {
    const val GEPriceData = "Module:GEPrices/data"
    const val GEVolumesData = "Module:GEVolumes/data"
    const val LastPricesData = "Module:LastPrices/data"
    const val QuestRequirementData = "Module:Questreq/data"
}

fun OsrsWiki.getQuestRequirements(): Map<String, List<QuestRequirement>> {
    val map = mutableMapOf<String, MutableList<QuestRequirement>>()

    val (_, result) = scribuntoSession.sendRequest {
        "questReqs" `=` require(WikiModules.QuestRequirementData).local()
        +"printReturn(questReqs)"
    }
    val list = result.asJsonObject.asMap()

    list.forEach { (key, value) ->
        val requirements = mutableListOf<QuestRequirement>()
        val quests = value.asJsonObject.getAsJsonArray("quests")
        val skills = value.asJsonObject.getAsJsonArray("skills")

        quests.forEach {
            println("    Quest: $it")
            requirements.add(QuestRequirement.Quest(it.asString))
        }

        skills.forEach skillLoop@ {
            println("    Skill: $it")
            val skill = it.asJsonArray
            if (skill.isEmpty) return@skillLoop
            val skillName = skill[0].asString
            when {
                skillName == "Kudos" -> requirements.add(QuestRequirement.Kudos(skill[1].asInt))
                skillName == "Quest point" -> requirements.add(QuestRequirement.QuestPoint(skill[1].asInt))
                skillName == "Combat" -> requirements.add(QuestRequirement.CombatLevel(skill[1].asInt))
                skillName.contains("Favour", true) ->
                    requirements.add(
                        QuestRequirement.Favor(
                            skillName.substring(0, skillName.length - 7).trim(),
                            skill[1].asString.substring(0, skill[1].asString.length - 1).toInt()
                        )
                    )
                skillName.contains("Barbarian Assault:") -> {
                    val name = skillName.substring(19)
                    val level = skill[1].asInt
                    requirements.add(QuestRequirement.BarbarianAssault(name, level))
                }
                skillName.isSkillName() -> {
                    val skillLevel = skill[1].asInt
                    var ironman = false
                    var boostable = false
                    if (skill.size() >= 3) {
                        val thirdArg = skill[2].asString
                        if (thirdArg == "ironman") ironman = true
                        else if (thirdArg == "boostable" || thirdArg == "boosted") boostable = true
                    }
                    if (skill.size() >= 4) {
                        val fourthArg = skill[3].asString
                        if (fourthArg == "ironman") ironman = true
                        else if (fourthArg == "boostable" || fourthArg == "boosted") boostable = true
                    }
                    requirements.add(QuestRequirement.Skill(skillName, skillLevel, boostable, ironman))
                }
                else -> {
                    println("Unknown skill: $skillName")
                }
            }
        }
        if (requirements.isNotEmpty()) map[key] = requirements
    }
    return map
}

fun OsrsWiki.getLastRevisionTimestamp(titles: Collection<String>): Map<String, String> =
    getNonContinuous(titles, WikiQuery.LastRevisionTimestamp, emptyMap(), "revisions")
        .mapValues { (_, value) ->
            if (value == null || value.isJsonNull) ""
            else {
                if (value.isJsonArray) {
                    val array = value.asJsonArray
                    if (array.size() == 0) ""
                    else array[0].asJsonObject.get("timestamp").asString
                } else value.asJsonObject.get("timestamp").asString
            }
        }



fun OsrsWiki.getLastRevisionTimestamp(vararg titles: String) = getLastRevisionTimestamp(titles.toList())
fun OsrsWiki.getPageTitlesFromIds(ids: Collection<Int>): Map<Int, String> {
    val result = mutableMapOf<Int, String>()
    for (id in ids) {
        val response = client.newCall("https://oldschool.runescape.wiki/w/Special:Lookup?type=item&id=$id")
        val url = response.networkResponse?.request?.url?.toString() ?: ""
        if (url.isEmpty()) {
            println("Failed to get page title for id $id")
            continue
        }
        val title = url.substringAfter("https://oldschool.runescape.wiki/w/").replace('_', ' ')
        result[id] = title
    }
    return result
}

fun OsrsWiki.getPageTitlesFromIds(vararg ids: Int) = getPageTitlesFromIds(ids.toList())
fun OsrsWiki.getPageTitleFromId(id: Int): String = getPageTitlesFromIds(listOf(id)).let { return it[id] ?: "" }
fun OsrsWiki.getItemPrice(id: Int): WikiItemPrice? {
    val response = client.newCall(WikiItemPrice.ItemPriceUrl, mapOf("id" to "$id")).body?.string() ?: ""
    if (response.isEmpty()) {
        println("Failed price request for item id: $id")
        return null
    }
    val result = GSON.fromJson(response, WikiItemPrice.WikiResponse::class.java)
    return WikiItemPrice(result.data[id]!!)
}
fun OsrsWiki.getItemBuyLimits(): ItemBuyLimits = ItemBuyLimits.fetch()
fun OsrsWiki.getUnalchableItemTitles(): List<String> = getTitlesInCategory("Items that cannot be alchemised")


fun OsrsWiki.dplAsk(query: Map<String, Any>): JsonElement {
    val (success, response) = scribuntoSession.sendRequest {
        "query" `=` query.local()
        +"dplAsk(query, true)"
    }
    return response
}

fun OsrsWiki.smwAsk(query: List<String>): JsonElement {
    val (success, response) = scribuntoSession.sendRequest {
        "query" `=` query.local()
        +"smwAsk(query, true)"
    }
    return response
}

fun OsrsWiki.getTemplatesOnPage(title: String): List<String> {
    val (success, response) = scribuntoSession.sendRequest {
        +"getTemplatesOnPage(\"$title\", true)"
    }
    return responseToArray(response).map { it.asString }
}

fun OsrsWiki.getTitlesInCategory(vararg categories: String): List<String> {
    val list = mutableListOf<String>()
    val chunkSize = 10000
    for (category in categories) {
        var offset = 0
        while (true) {
            val (success, response) = scribuntoSession.sendRequest {
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

fun OsrsWiki.getAllItemTitles(): List<String> = getTitlesInCategory("Items", "Pets")

fun OsrsWiki.getExchangeData(itemName: String): WikiExchangeData {
    val (success, response) = scribuntoSession.sendRequest {
        +"loadExchangeData({\"$itemName\"}, true)"
    }
    return GSON.fromJson(response, WikiExchangeData::class.java)
}

@OptIn(DelicateCoroutinesApi::class)
fun OsrsWiki.getExchangeData(itemNames: List<String>): Map<String, WikiExchangeData> {
    val map = mutableMapOf<String, WikiExchangeData>()
    val chunkSize = 200
    val queue = TitleQueue(itemNames, chunkSize)
    val main = GlobalScope.launch {
        with (queue) {
            process { list ->
                val (success, response) = scribuntoSession.sendRequest {
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

fun OsrsWiki.getExchangeData(vararg itemNames: String): Map<String, WikiExchangeData> =
    getExchangeData(itemNames.toList())

fun OsrsWiki.getAllExchangeData(): Map<String, WikiExchangeData> {
    val titles = getAllItemTitles()
    return getExchangeData(titles)
}

fun OsrsWiki.getItemDetails(itemName: String): List<ItemDetails> {
    val (success, result) = scribuntoSession.sendRequest {
        +"loadItemData({\"$itemName\"}, true)"
    }
    val itemResult = result.asJsonObject[itemName]
    return ItemDetails.fromJsonElement(itemResult).second
}

@OptIn(DelicateCoroutinesApi::class)
fun OsrsWiki.getItemDetails(itemNames: List<String>): Map<String, List<ItemDetails>> {
    val map = mutableMapOf<String, MutableList<ItemDetails>>()
    val chunkSize = 100
    val queue = TitleQueue(itemNames, chunkSize)
    val main = GlobalScope.launch {
        with (queue) {
            process { list ->
                val (success, result) = scribuntoSession.sendRequest {
                    "sessionData" `=` list
                    +"loadItemData(sessionData, true)"
                }
                if (!success) list
                else {
                    if (result.isJsonArray) {
                        val array = result.asJsonArray
                        if (array.isEmpty) emptyList()
                        else throw IllegalStateException("Unhandled JsonArray Response from Scribunto: $result")
                    } else {
                        val data = result.asJsonObject
                        for (title in list) {
                            val titleResult = data.getAsJsonObject(title)
                            val details = ItemDetails.fromJsonElement(titleResult)
                            if (details.first.isNotEmpty() && details.second.isNotEmpty()) {
                                val titleList = map.getOrPut(details.first) { mutableListOf() }
                                titleList.addAll(details.second)
                            }
                        }
                        emptyList()
                    }
                }
            }
        }
    }
    runBlocking { main.join() }
    return map
}

fun OsrsWiki.getItemDetails(vararg itemNames: String): Map<String, List<ItemDetails>> =
    getItemDetails(itemNames.toList())



fun OsrsWiki.getAllItemDetails(): Map<String, List<ItemDetails>> {
    val titles = getAllItemTitles()
    return getItemDetails(titles)
}

fun OsrsWiki.getAllRevisionsSince(date: Date, categories: Collection<String>): Map<String, Date> {
    val map = mutableMapOf<String, Date>()
    dplAsk(mapOf(
        "category" to
    ))

}

fun OsrsWiki.getTitlesWithLocationData(): List<String> {
    val returnList = mutableListOf<String>()
    runChunks(500) { chunkSize, offset ->
        val (success, request) = scribuntoSession.sendRequest {
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

fun OsrsWiki.getLocationJson(title: String): LocationDetails? {
    val (success, request) = scribuntoSession.sendRequest {
        +"loadLocationDataByTitle(\"$title\", true)"
    }
    val obj = if (request.isJsonArray) {
        val array = request.asJsonArray
        if (array.isEmpty) return null
        else array[0].asJsonObject
    } else request.asJsonObject
    val locationJson = JsonParser.parseString(obj["Location JSON"].asString)
    return LocationDetails.fromJsonElement(locationJson)
}

fun OsrsWiki.getLocationJson(): Map<String, List<LocationDetails>> {
    val results = mutableMapOf<String, MutableList<LocationDetails>>()
    runChunks(500) { chunkSize, offset ->
        val (success, response) = scribuntoSession.sendRequest {
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
                    val details = LocationDetails.fromJsonElement(JsonParser.parseString(it.asString))
                    addLocation(cleanedName, details)
                }
            } else {
                val details = LocationDetails.fromJsonElement(JsonParser.parseString(json.asString))
                addLocation(cleanedName, details)
            }
        }
        responseSize
    }
    return results
}


fun runChunks(chunkSize: Int, worker: (Int, Int) -> Int) {
    var offset = 0
    while (true) {
        val processedAmount = worker(chunkSize, offset)
        offset += processedAmount
        if (processedAmount < chunkSize) break
    }
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


private fun OsrsWiki.parseContinuousToSingle(
    titles: Collection<String>,
    template: WikiQuery.Template,
    key: String,
    properties: Map<String, String> = emptyMap(),
    templateKey: String? = null
) = if (templateKey == null) parsePropertiesToSingle(getContinuous(titles, template, properties, key))
else parsePropertiesToSingle(getContinuous(titles, template, properties, key), templateKey)
private fun parsePropertiesToSingle(map: Map<String, List<JsonObject?>>, key: String): Map<String, List<String>> =
    map.mapValues { (_, value) -> value.mapNotNull { it?.getString(key) } }
private fun parsePropertiesToSingle(map: Map<String, List<JsonObject?>>) = parsePropertiesToSingle(map, "title")
private fun parsePropertiesToDouble(
    map: Map<String, List<JsonObject?>>,
    firstKey: String,
    secondKey: String
): Map<String, List<Pair<String?, String?>>> =
    map.mapValues { (_, value) ->
        value.map { (it?.getString(firstKey) to it?.getString(secondKey)) }.toMutableList()
    }

private fun OsrsWiki.processBulk(
    titles: Collection<String>,
    worker: (List<String>) -> Unit
) {
    val queue = GroupQueue(titles, configuration.groupQueryLimit)
    var completed = 0
    val start = System.currentTimeMillis()
    if (titles.size > 10) {
        sharedLoad(
            jobs = minOf(titles.size, configuration.maxRequestJobs),
            update = {
                val percent = (completed / titles.size.toDouble() * 100).toInt()
                print("\rProcessing Bulk Request: $completed of ${titles.size} ($percent%) - $it seconds")
            },
        ) {
            var next: List<String> = queue.syncPoll()
            while (next.isNotEmpty()) {
                worker(next)
                completed += next.size
                next = queue.syncPoll()
            }
        }
    } else {
        while (queue.isNotEmpty()) {
            val next = queue.poll()
            worker(next)
            completed += next.size
        }
    }
    println("\rProcessed Bulk Request of ${titles.size} titles in ${(System.currentTimeMillis() - start).toSecondsString()}")
}

private fun OsrsWiki.getContinuous(
    titles: Collection<String>,
    template: WikiQuery.Template,
    properties: Map<String, String>,
    key: String
): Map<String, List<JsonObject>> {
    val result = mutableMapOf<String, MutableList<JsonObject>>()
    if (titles.isEmpty()) return result

    processBulk(titles) { list ->
        val query = newQuery(template)
        query["titles"] = list
        if (properties.isNotEmpty()) properties.forEach(query::set)
        while (query.isNotEmpty()) {
            query.next()!!.propertyComprehension("title", key).forEach { (k, value) ->
                result[k] = value?.asJsonArray?.toJsonObjectsList() ?: mutableListOf()
            }
        }
    }
    return result
}

private fun OsrsWiki.getNonContinuous(
    titles: Collection<String>,
    template: WikiQuery.Template,
    properties: Map<String, String>,
    key: String
): MutableMap<String, JsonElement?> {
    val result = mutableMapOf<String, JsonElement?>()
    if (titles.isEmpty()) return result
    processBulk(titles) { list ->
        val query = newQuery(template)
        query["titles"] = list
        if (properties.isNotEmpty()) properties.forEach(query::set)
        query.next()!!.propertyComprehension("title", key).forEach { (k, value) ->
            result[k] = value
        }
    }
    return result
}

private fun OsrsWiki.getNonContinuousList(
    titles: Collection<String>,
    template: WikiQuery.Template,
    properties: Map<String, String>,
    key: String,
    templateKey: String
): List<JsonObject> {
    val result = mutableListOf<JsonObject>()
    if (titles.isEmpty()) return result
    processBulk(titles) { list ->
        val query = newQuery(template)
        query[templateKey] = list
        if (properties.isNotEmpty()) properties.forEach(query::set)
        result.addAll(query.next()!!.listComprehension(key))
    }
    return result
}

private fun OsrsWiki.getContinuousList(
    titles: Collection<String>,
    template: WikiQuery.Template,
    properties: Map<String, String>,
    key: String,
    templateKey: String
): List<JsonObject> {
    val result = mutableListOf<JsonObject>()
    if (titles.isEmpty()) return result
    processBulk(titles) { list ->
        val query = newQuery(template)
        query[templateKey] = list
        if (properties.isNotEmpty()) properties.forEach(query::set)
        while (query.isNotEmpty()) {
            result.addAll(query.next()!!.listComprehension(key))
        }
    }
    return result
}

@OptIn(DelicateCoroutinesApi::class)
private fun sharedLoad(
    jobs: Int = 10,
    updateInterval: Int = 100,
    update: (Long) -> Unit = { _ -> },
    worker: suspend (Int) -> Unit
) = runBlocking {
    val jobsList = mutableListOf<Job>()
    val started = System.currentTimeMillis()
    GlobalScope.launch {
        repeat(jobs) {
            val job = launch {
                worker(it)
            }
            jobsList.add(job)
        }
        val updater = launch {
            delay(updateInterval.toLong())
            update(System.currentTimeMillis() - started)
        }
        jobsList.add(updater)
        jobsList.joinAll()
    }.join()
}