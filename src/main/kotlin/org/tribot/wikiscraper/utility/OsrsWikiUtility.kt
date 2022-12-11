package org.tribot.wikiscraper.utility

import com.google.gson.*
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.apache.commons.text.StringEscapeUtils
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*


/* Written by IvanEOD 12/9/2022, at 8:28 AM */

internal val InstantDeserializer: JsonDeserializer<Instant> =
    JsonDeserializer { j, _, _ -> Instant.parse(j.asJsonPrimitive.asString) }

internal val HttpUrlDeserializer: JsonDeserializer<HttpUrl> =
    JsonDeserializer { j, _, _ -> j.asJsonPrimitive.asString.toHttpUrlOrNull()!! }

internal val GSON: Gson = GsonBuilder()
    .registerTypeAdapter(Instant::class.java, InstantDeserializer)
    .registerTypeAdapter(HttpUrl::class.java, HttpUrlDeserializer)
    .create()

internal val WikiDoubleBracketDateRegex = "^\\[\\[(\\d+)\\s(\\w+)]]\\s\\[\\[(\\d+)]]".toRegex()
val WikiDoubleBracketDateFormatter = SimpleDateFormat("[[dd MMMM]] [[yyyy]]")
val WikiAlternateDateFormatter = SimpleDateFormat("dd MMMM yyyy")

fun Date.toWikiDoubleBracketFormat(): String = WikiDoubleBracketDateFormatter.format(this)
fun Date.toWikiAlternateFormat(): String = WikiAlternateDateFormatter.format(this)

fun Long.toSecondsString(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val years = days / 365
    return when {
        years > 0 -> "${years}y ${days % 365}d"
        days > 0 -> "${days}d ${hours % 24}h"
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}

val Number.msSeconds: Long get() = this.toLong() * 1000
val Number.msMinutes: Long get() = this.toLong() * 60 * 1000
val Number.msHours: Long get() = this.toLong() * 60 * 60 * 1000

fun Long.toDate(format: String): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(format)
    return formatter.format(date)
}
internal fun String.urlEncode() = java.net.URLEncoder.encode(this, "UTF-8")
internal fun String.htmlUnescape(): String = StringEscapeUtils.unescapeHtml4(this)
internal fun String.toJsonObject(): JsonObject? = try {
    JsonParser.parseString(this).asJsonObject
} catch (e: Exception) {
    null
}
fun Regex.extractMatches(text: String) = findAll(text).map { it.value }.toList()
fun Regex.getAllMatchGroups(text: String): List<String> {
    val matcher = toPattern().matcher(text)
    val matches = mutableListOf<String>()
    while (matcher.find()) {
        for (i in 1..matcher.groupCount()) matches.add(matcher.group(i))
    }
    return matches
}
internal fun JsonArray.toJsonObjectsList(): MutableList<JsonObject> = map { it.asJsonObject }.toMutableList()
internal fun JsonObject.toJsonObjectsList(): MutableList<JsonObject> =
    entrySet().map { it.value.asJsonObject }.toMutableList()

internal fun JsonObject.toJsonObjectsList(key: String): MutableList<JsonObject> {
    val obj = get(key)
    return if (obj.isJsonObject) obj.asJsonObject.toJsonObjectsList()
    else if (obj.isJsonArray) obj.asJsonArray.toJsonObjectsList()
    else {
        println("Invalid object type for key $key: ${obj.javaClass}")
        obj.asJsonObject.toJsonObjectsList()
    }
}

internal fun JsonObject.getString(key: String): String =
    if (!has(key)) "" else get(key).let { if (it.isJsonPrimitive) it.asString else "" }

internal fun JsonObject.hasNested(vararg keys: String): Boolean {
    if (keys.isEmpty()) return false
    var last: JsonObject = this
    try {
        for (key in keys) last = last.getAsJsonObject(key)
    } catch (e: Exception) {
        return false
    }
    return last.has(keys.last())
}

internal fun JsonObject.getNestedJsonObject(vararg keys: String): JsonObject? {
    if (keys.isEmpty()) return null
    var last: JsonObject = this
    try {
        for (key in keys) last = last.getAsJsonObject(key)
    } catch (e: Exception) {
        return null
    }
    return last
}
internal fun JsonObject.toStringMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    entrySet().forEach { map[it.key] = it.value.asString }
    return map
}
internal fun JsonObject.toVersionedMap(): VersionedMap {
    val templateProperties = mutableListOf<TemplateProperty>()
    TemplateProperty.parse(this.keySet()).forEach(templateProperties::add)
    val versionCount = templateProperties.maxOfOrNull { maxOf(it.getKeyVersions().size, 1) } ?: 1
    val templatePropertyDataList = mutableListOf<TemplatePropertyData>()
    templateProperties.forEach { property ->
        val key = property.getKey()
        val versions = property.getKeyVersions()
        var value = getString(key)
        val values = mutableListOf<TemplatePropertyData>()
        if (value.isNotBlank()) values.add(TemplatePropertyData(property.name, key, true, 0, value))
        else if (versions.isNotEmpty()) {
            val list = mutableListOf<String>()
            versions.forEachIndexed { index, versionKey ->
                value = getString(versionKey)
                if (value.isNotBlank()) values.add(TemplatePropertyData(property.name, versionKey, true, index + 1, value))
                list.add(value)
            }
            values.add(TemplatePropertyData(property.name, key, false, 0, list.joinToString(",")))
        }
        values.sortBy { it.version }
        templatePropertyDataList.addAll(values)
    }
    return VersionedMap(versionCount, templatePropertyDataList)
}

internal fun JsonObject.getNestedJsonArray(vararg keys: String): JsonArray = if (keys.isEmpty()) JsonArray()
else if (keys.size == 1) this.getAsJsonArray(keys[0])
else getNestedJsonObject(*keys.dropLast(1).toTypedArray())?.getAsJsonArray(keys.last()) ?: JsonArray()

internal fun Iterable<JsonObject>.mappedBy(keyKey: String, valueKey: String): MutableMap<String, String> =
    this.associate { it.getString(keyKey) to it.getString(valueKey) }.toMutableMap()

internal fun Iterable<String>.pipeFence() = joinToString("|")
internal fun Iterable<String>.pipeFence(beforeString: String = "", afterString: String = "") =
    joinToString("|") { "$beforeString$it$afterString" }


internal fun propertyMap(vararg parameters: String): MutableMap<String, String> =
    if (parameters.size % 2 == 1) throw IllegalArgumentException("Property map must have an even number of parameters.")
    else parameters.toList().chunked(2).associate { it[0] to it[1] }.toMutableMap()

val skillNames = listOf(
    "Attack",
    "Defence",
    "Strength",
    "Hitpoints",
    "Ranged",
    "Prayer",
    "Magic",
    "Cooking",
    "Woodcutting",
    "Fletching",
    "Fishing",
    "Firemaking",
    "Crafting",
    "Smithing",
    "Mining",
    "Herblore",
    "Agility",
    "Thieving",
    "Slayer",
    "Farming",
    "Runecraft",
    "Hunter",
    "Construction",
)



fun String.isSkillName(): Boolean = skillNames.contains(this) || skillNames.contains(this.lowercase())

inline fun <reified T : Any> String.getNullable(): T? = when (T::class) {
    String::class -> this as T
    Int::class -> this.toIntOrNull() as? T
    Long::class -> this.toLongOrNull() as? T
    Double::class -> this.toDoubleOrNull() as? T
    Boolean::class -> {
        when (this.lowercase()) {
            "yes", "y", "true", "t", "on", "1" -> true as T
            "no", "n", "false", "f", "off", "0" -> false as T
            else -> this.toBooleanStrictOrNull() as? T
        }
    }
    Date::class -> getDateNullable() as? T
    else -> null
}

val DefaultDate: Date = Date.from(Instant.now().minus(3650 * 20, ChronoUnit.DAYS))

fun String.getDateNullable(formatter: SimpleDateFormat? = null): Date? {
    if (this.isBlank()) return null
    var date: Date? = null
    if (formatter != null) date = runCatching { formatter.parse(this) }.getOrNull()
    if (date == null) date = runCatching { WikiDoubleBracketDateFormatter.parse(this) }.getOrNull()
    if (date == null) date = runCatching { WikiAlternateDateFormatter.parse(this) }.getOrNull()
    if (date == null) date = runCatching { Date.from(Instant.parse(this)) }.getOrNull()
    if (date == null) date = runCatching { Date.from(Instant.ofEpochMilli(this.toLong())) }.getOrNull()
//    println("Parsing date: $this = ${date?.toWikiAlternateFormat() ?: "null"}")
    return date
}


fun String.getDateNonNullable(defaultValue: Date? = null, formatter: SimpleDateFormat? = null) =
    this.getDateNullable(formatter) ?: defaultValue ?: DefaultDate

fun String.getDateRangeNullable(): ClosedRange<Date>? {
    if (this.isEmpty()) return null
    val split = this.split(" - ")
    if (split.size != 2) return null
    val formatter = WikiAlternateDateFormatter
    val start = split[0].getDateNullable(formatter = formatter)
    val end = split[1].getDateNullable(formatter = formatter)
    if (start == null || end == null) return null
    return start..end
}

inline fun <reified T : Any> String.getNonNullable(defaultValue: T? = null): T = when (T::class) {
    String::class -> this as T
    Int::class -> getNullable() ?: defaultValue ?: 0 as T
    Long::class -> getNullable() ?: defaultValue ?: 0L as T
    Double::class -> getNullable() ?: defaultValue ?: 0.0 as T
    Boolean::class -> getNullable() ?: defaultValue ?: false as T
    Date::class -> getNullable() ?: defaultValue ?: DefaultDate as T
    else -> {
        if (defaultValue != null) {
            println("String.getNonNullable() Returning default value for ${T::class.simpleName} because no parsing method was supported.")
            println("String: $this")
            defaultValue
        } else throw IllegalArgumentException("Unsupported type: ${T::class}")
    }
}