package org.tribot.wikiscraper.query

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import org.tribot.wikiscraper.OsrsWiki
import org.tribot.wikiscraper.classes.ItemBuyLimits
import org.tribot.wikiscraper.classes.ItemDetails
import org.tribot.wikiscraper.classes.QuestRequirement
import org.tribot.wikiscraper.classes.WikiItemPrice
import org.tribot.wikiscraper.utility.*
import java.net.URLEncoder
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar


/* Written by IvanEOD 12/9/2022, at 8:36 AM */


fun OsrsWiki.getQuestRequirements(): Map<String, List<QuestRequirement>> {
    val map = mutableMapOf<String, MutableList<QuestRequirement>>()
    val request = scribuntoConsole(
        "local questReqs = require('Module:Questreq/data')\n" +
                "local stringValue = mw.text.jsonEncode(questReqs)\n" +
                "print(stringValue)"
    )
    val json = JsonParser.parseString(request).asJsonObject
    val print = json.get("print").asString.htmlUnescape()
    val printJson = JsonParser.parseString(print).asJsonObject
    val list = printJson.asMap()

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


@OptIn(DelicateCoroutinesApi::class)
fun OsrsWiki.getItemTemplates(): Map<String, ItemDetails> {
    val properties = mutableMapOf(
        "namespace" to "",
        "uses" to "Template:Infobox Item",
        "count" to 500,
        "offset" to 0,
        "include" to "{Infobox Item}, {Infobox Bonuses}",
        "ignorecase" to true
    )
    val templates = mutableMapOf<String, Pair<JsonObject, JsonObject?>>()

    println("Getting all item infoboxes...")
    val start = System.currentTimeMillis()
    runBlocking {
        GlobalScope.launch {
            (0..60).map { iteration ->
                async {
                    val props = properties.toMutableMap()
                    props["offset"] = 500 * iteration
                    val result = scribuntoAskRequest(props)
                    if (result.isEmpty()) return@async emptyList<Pair<String, Pair<JsonObject, JsonObject?>>>()
                    result.map {
                        val title = it["title"].asString
                        val infoboxItem = it["include"].asJsonObject["Infobox Item"].asJsonObject
                        val infoboxBonuses = it["include"].asJsonObject["Infobox Bonuses"]

                        if (infoboxBonuses != null) {
                            when (infoboxBonuses) {
                                is JsonArray -> {
                                    val array = infoboxBonuses.asJsonArray
                                    if (array.isEmpty) title to (infoboxItem to null)
                                    else title to (infoboxItem to array[0].asJsonObject)
                                }
                                is JsonObject -> {
                                    val obj = infoboxBonuses.asJsonObject
                                    title to (infoboxItem to obj)
                                }
                                else -> title to (infoboxItem to null)
                            }
                        } else title to (infoboxItem to null)
                    }
                }
            }.awaitAll().flatten().forEach { (title, pair) -> templates[title] = pair }

        }.join()
    }
    println("Got ${templates.size} item details in ${(System.currentTimeMillis() - start).toSecondsString()}.")
    val buyLimits = getItemBuyLimits()
    val unalchableItems = getUnalchableItemTitles()
    val timestamps = getLastRevisionTimestamp(templates.keys)

    val itemDetails: MutableMap<String, ItemDetails> = mutableMapOf()
    templates.forEach { (title, template) ->

        val itemTemplate = template.first.toVersionedMap()
        val bonusesTemplate = template.second?.toVersionedMap()
        val itemVersions = itemTemplate.getIndividualVersions()
        val bonusesVersions = bonusesTemplate?.getIndividualVersions() ?: emptyList()

        for (i in 0..itemVersions.size) {
            val itemData = itemVersions.getOrNull(i)?.toMutableMap() ?: continue
            val bonusesData = bonusesVersions.getOrNull(i) ?: bonusesVersions.firstOrNull() ?: emptyMap()
            itemData["alchable"] = if (title !in unalchableItems) "yes" else "no"
            itemData["lastupdate"] = timestamps[title] ?: ""
            itemData["buylimit"] = buyLimits[title]?.toString() ?: ""
            itemDetails[title] = ItemDetails.fromMap(itemData, bonusesData)
        }
    }
    println("Built ${templates.keys.size} templates in ${(System.currentTimeMillis() - start).toSecondsString()}.")
    return itemDetails
}

//fun OsrsWiki.getItemTemplates(vararg titles: String): Map<String, ItemDetails> {
//
//}



private fun OsrsWiki.getCategoryMembers(categories: Collection<String>, vararg namespaces: OsrsWiki.Namespace): List<String> {
    val result = mutableListOf<String>()
    for (category in categories) {
        val query = newQuery(WikiQuery.CategoryMembers)
        query["cmtitle"] = namespaceManager.convertIfNotInNamespace(category, OsrsWiki.Namespace.Category)
        if (namespaces.isNotEmpty()) query["cmnamespace"] = namespaceManager.createFilter(*namespaces)
        while (query.isNotEmpty())
            result.addAll(query.next()!!
                .listComprehension("categorymembers")
                .map { it.asJsonObject.getString("title") })
    }
    return result.distinct()
}

fun OsrsWiki.getTitlesInCategory(categories: Collection<String>): List<String> =
    getCategoryMembers(categories).filter {
        !it.startsWith("Category:") && !it.startsWith("File:") && it !in categories
    }

fun OsrsWiki.getTitlesInCategory(vararg categories: String) = getTitlesInCategory(categories.toList())

fun OsrsWiki.scribuntoConsole(code: String): String = client.newCall(
    "https://oldschool.runescape.wiki/api.php?action=scribunto-console&format=json&title=Var&question=${
        URLEncoder.encode(code, "UTF-8")
    }"
).body?.string() ?: ""


//    namespace = '',
//    uses = 'Template:Infobox Item',
//    count = 1,
//    offset = 1,
//    include = '{Infobox Item}',
//    ignorecase = true

// mapOf(
//    "namespace" to "",
//    "uses" to "Template:Infobox Item",
//    "count" to 1,
//    "offset" to 1,
//    "include" to "{Infobox Item}",
//    "ignorecase" to true
// )
//https://oldschool.runescape.wiki/w/Module:DPLlua

fun OsrsWiki.scribuntoAskRequest(properties: Map<String, Any>): List<JsonObject> {
    val propertiesString =
        properties.entries.joinToString(",\n") { "${it.key} = ${if (it.value is String) "'${it.value}'" else it.value}" }
    val script =
        "local dpl = require( 'Module:DPLlua' )\nlocal a = dpl.ask({\n$propertiesString\n})\nprint(mw.text.jsonEncode(a))"
    val requestPrint = JsonParser.parseString(scribuntoConsole(script).htmlUnescape())
        .asJsonObject.getString("print").htmlUnescape()
    if (requestPrint.isEmpty()) return emptyList()
    val jsonObject = JsonParser.parseString(requestPrint)?.asJsonObject ?: return emptyList()
    jsonObject.remove("DPL time")
    jsonObject.remove("Parse time")
    return jsonObject.toJsonObjectsList()
}


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