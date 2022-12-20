@file:OptIn(DelicateCoroutinesApi::class)

package scripts.wikiscraper.query

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import scripts.wikiscraper.OsrsWiki
import scripts.wikiscraper.classes.*
import scripts.wikiscraper.lua.TitleQueue
import scripts.wikiscraper.utility.*
import scripts.wikiscraper.utility.GSON
import scripts.wikiscraper.utility.GroupQueue
import scripts.wikiscraper.utility.getString
import scripts.wikiscraper.utility.pipeFence
import java.text.SimpleDateFormat
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

private fun OsrsWiki.getTitleFromIds(map: Map<Int, String>): Map<Pair<Int, String>, String> {
    val result = mutableMapOf<Pair<Int, String>, String>()

    for ((id, type) in map) {
        val response = client.newCall("https://oldschool.runescape.wiki/w/Special:Lookup?type=$type&id=$id")
        val url = response.networkResponse?.request?.url?.toString() ?: ""
        if (url.isEmpty()) {
            println("Failed to get page title for id $id")
            continue
        }
        val title = url.substringAfter("https://oldschool.runescape.wiki/w/").replace('_', ' ')
        result[id to type] = title
    }
    return result
}

fun OsrsWiki.getLastRevisionTimestamp(vararg titles: String) = getLastRevisionTimestamp(titles.toList())
fun OsrsWiki.getItemPageTitlesFromIds(ids: Collection<Int>): Map<Int, String> {
    val map = ids.associateWith { "item" }
    return getTitleFromIds(map).map { (key, value) -> key.first to value }.toMap()
}
fun OsrsWiki.getNpcPageTitlesFromIds(ids: Collection<Int>): Map<Int, String> {
    val map = ids.associateWith { "npc" }
    return getTitleFromIds(map).map { (key, value) -> key.first to value }.toMap()
}
fun OsrsWiki.getObjectPageTitlesFromIds(ids: Collection<Int>): Map<Int, String> {
    val map = ids.associateWith { "object" }
    return getTitleFromIds(map).map { (key, value) -> key.first to value }.toMap()
}

fun OsrsWiki.getItemPageTitlesFromIds(vararg ids: Int) = getItemPageTitlesFromIds(ids.toList())
fun OsrsWiki.getNpcPageTitlesFromIds(vararg ids: Int) = getNpcPageTitlesFromIds(ids.toList())
fun OsrsWiki.getObjectPageTitlesFromIds(vararg ids: Int) = getObjectPageTitlesFromIds(ids.toList())
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
    val (success, response) = bulkScribunto {
        "query" `=` query.local()
        +"dplAsk(query, true)"
    }
    return response
}

fun OsrsWiki.smwAsk(query: List<String>): JsonElement {
    val (success, response) = bulkScribunto {
        "query" `=` query.local()
        +"smwAsk(query, true)"
    }
    return response
}

fun OsrsWiki.getTemplatesOnPage(title: String): List<String> {
    val (success, response) = bulkScribunto {
        +"getTemplatesOnPage(\"$title\", true)"
    }
    return responseToArray(response).map { it.asString }
}

fun OsrsWiki.getAllTitlesUsingTemplate(vararg templates: String): List<String> {
    val cleanedTemplates = templates.map {
        if (!it.startsWith("Template:", true)) "Template:$it"
        else it
    }
    val returnList = runChunks(500) {
        val response = dplAsk(mapOf(
            "namespace" to "",
            "uses" to cleanedTemplates.pipeFence(),
            "count" to chunkSize,
            "offset" to offset
        ))
        val results = responseToArray(response)
        results.size() to results.map { it.asString }
    }
    return returnList.flatten().distinct()
}

fun OsrsWiki.getTitlesInCategory(vararg categories: String): List<String> {
    val cleanedCategories = categories.map {
        if (it.startsWith("Category:", true)) it.removePrefix("Category:")
        else it
    }
    val returnList = runChunks(500) {
        val response = dplAsk(mapOf(
            "namespace" to "",
            "category" to cleanedCategories.pipeFence(),
            "count" to chunkSize,
            "offset" to offset,
            "ignorecase" to true,
        ))
        val results = responseToArray(response)
        results.size() to results.map { it.asString }.filter { it !in cleanedCategories }
    }
    return returnList.flatten().distinct()
}

fun OsrsWiki.getAllItemTitles(): List<String> = getAllTitlesUsingTemplate("Infobox Item")

fun OsrsWiki.getExchangeData(itemName: String): WikiExchangeData {
    val (success, response) = scribuntoSession.sendRequest {
        +"loadExchangeData({\"$itemName\"}, true)"
    }
    return GSON.fromJson(response, WikiExchangeData::class.java)
}

fun OsrsWiki.getExchangeData(itemNames: List<String>): Map<String, WikiExchangeData> {
    val map = mutableMapOf<String, WikiExchangeData>()
    val chunkSize = 200
    val queue = TitleQueue(itemNames, chunkSize)
    val main = GlobalScope.launch {
        with (queue) {
            execute { list ->
                val (success, response) = bulkScribunto {
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
    val itemResult = result.asJsonObject.getAsJsonObject(itemName)
    return ItemDetails.fromJsonElement(itemResult).second
}

fun OsrsWiki.loadAllItemData(itemNames: List<String>): Map<String, List<JsonObject>> {
    val map = mutableMapOf<String, MutableList<JsonObject>>()
    val chunkSize = 100
    val queue = TitleQueue(itemNames, chunkSize)
    val main = GlobalScope.launch {
        with (queue) {
            execute { list ->
                val (success, result) = bulkScribunto {
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
                            map.getOrPut(title) { mutableListOf() }.add(titleResult)
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

fun OsrsWiki.loadAllItemData(vararg itemNames: String): Map<String, List<JsonObject>> =
    this@loadAllItemData.loadAllItemData(itemNames.toList())

fun OsrsWiki.loadAllItemData(): Map<String, List<JsonObject>> {
    val titles = getAllItemTitles()
    return loadAllItemData(titles)
}

fun OsrsWiki.getItemDetails(itemNames: List<String>): Map<String, List<ItemDetails>> {
    val itemData = loadAllItemData(itemNames)
    val map = mutableMapOf<String, MutableList<ItemDetails>>()
    for ((title, data) in itemData) {
        data.forEach {
            val (name, details) = ItemDetails.fromJsonElement(it)
            if (name.isNotEmpty() && details.isNotEmpty()) map.getOrPut(name) { mutableListOf() }.addAll(details)
        }
    }
    return map
}

fun OsrsWiki.getAllItemDetails(): Map<String, List<ItemDetails>> {
    val titles = getAllItemTitles()
    return getItemDetails(titles)
}

fun OsrsWiki.getNpcDetails(vararg npcName: String): Map<String, List<NpcDetails>> {
    val titles = npcName.toList()
    val (success, result) = bulkScribunto {
        "data" `=` titles.local()
        +"loadNpcData(data, true)"
    }
    if (!success) return emptyMap()
    val map = mutableMapOf<String, MutableList<NpcDetails>>()
    val data = result.asJsonObject
    for (title in titles) {
        val titleResult = data.getAsJsonObject(title)
        val details = NpcDetails.fromJsonObject(titleResult)
        details.forEach { (title, list) ->
            map.getOrPut(title) { mutableListOf() }.addAll(list)
        }
    }
    return map
}

fun OsrsWiki.getMonsterDetails(vararg monsterName: String): Map<String, List<MonsterDetails>> {
    val titles = monsterName.toList()
    val (success, result) = bulkScribunto {
        "data" `=` titles.local()
        +"loadMonsterData(data, true)"
    }
    if (!success) return emptyMap()
    val map = mutableMapOf<String, MutableList<MonsterDetails>>()
    val data = result.asJsonObject
    println("Data")
    println(data)
    for (title in titles) {
        val titleResult = data.getAsJsonObject(title)
        println("Title Result: $titleResult")
        val resultsMap = MonsterDetails.fromJsonObject(titleResult)
        println("Results Map: $resultsMap")
        resultsMap.forEach { (name, list) ->
            if (name.isNotEmpty() && list.isNotEmpty())
                map.getOrPut(name) { mutableListOf() }.addAll(list)
        }
    }
    return map
}

fun OsrsWiki.getAllMonsterTitles(): List<String> = getAllTitlesUsingTemplate("Infobox Monster")
fun OsrsWiki.getAllNpcTitles(): List<String> = getAllTitlesUsingTemplate("Infobox NPC")


fun OsrsWiki.getAllTitlesWithRevisionsSince(date: Date, categories: Collection<String>): List<String> {
    val results = dplAsk(mapOf(
        "category" to categories.pipeFence(),
        "allrevisionssince" to SimpleDateFormat("yyyy-MM-dd").format(date)
    ))
    return responseToArray(results).map { it.asString }
}

fun OsrsWiki.getTitlesWithLocationData(): List<String> {
    val results = runChunks(500) {
        val list = mutableListOf<String>()
        val (success, request) = bulkScribunto {
            +"loadTitlesWithLocationData($chunkSize, $offset, true)"
        }
        val array = request.asJsonArray.map { cleanLocationName(it.asJsonArray[0].asString) }
        val returnSize = array.size
        list.addAll(array)
        returnSize to list
    }
    return results.flatten().distinct()
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
    val chunkedResults = runChunks(500) {
        val map = mutableMapOf<String, MutableList<LocationDetails>>()
        val (success, response) = scribuntoSession.sendRequest {
            +"loadLocationData($chunkSize, $offset, true)"
        }
        val responseArray = responseToArray(response)
        val responseSize = responseArray.size()
        fun addLocation(name: String, location: LocationDetails) {
            val list = map.getOrPut(name) { mutableListOf() }
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
        responseSize to map
    }
    chunkedResults.forEach { map ->
        map.forEach { (name, list) ->
            val existing = results.getOrPut(name) { mutableListOf() }
            existing.addAll(list)
            existing.distinct()
        }
    }
    return results
}

class ChunkScope<T>(val chunkSize: Int, val offset: Int) {
    var returnSize: Int = -1
    var result: T? = null
}

inline fun <reified T> runChunks(chunkSize: Int, crossinline worker: ChunkScope<T>.() -> Pair<Int, T>): List<T> {
    var offset = 0
    var cancelledOffset = 0
    var shouldContinue = true
    val offsets = mutableSetOf<Int>()
    val scopes = mutableListOf<ChunkScope<T>>()
    val job = GlobalScope.launch mainJob@ {
        val jobs = mutableListOf<Job>()
        while (shouldContinue) {
            if (offset !in offsets && jobs.count { !it.isCompleted && !it.isCancelled } < 10) {
                offsets += offset
                val scope = ChunkScope<T>(chunkSize, offset)
                scopes += scope
                jobs += launch jobLoop@ {
                    val (processedAmount, results) = scope.worker()
                    scope.result = results
                    scope.returnSize = processedAmount
                    if (scope.returnSize < chunkSize) {
                        if (cancelledOffset == 0) cancelledOffset = scope.offset
                        shouldContinue = false
                    }
                }
                offset += chunkSize
            } else delay(50)
        }
        jobs.joinAll()
    }
    runBlocking { job.join() }
    return scopes.filter { it.offset <= cancelledOffset && it.returnSize != 0 }.mapNotNull { it.result }
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