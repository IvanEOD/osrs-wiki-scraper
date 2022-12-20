package scripts.wikiscraper.classes

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import scripts.wikiscraper.utility.*
import scripts.wikiscraper.utility.toVersionedMap
import java.util.*


/* Written by IvanEOD 12/9/2022, at 9:14 AM */
data class ItemDetails(
    val id: Int,
    val name: String,
    val version: String,
    val image: String,
    val aka: List<String>,
    val members: Boolean,
    val alchable: Boolean,
    val equippable: Boolean,
    val tradeable: Boolean,
    val stackable: Boolean,
    val stacksInBank: Boolean,
    val placeholder: Boolean,
    val quest: List<String>,
    val destroy: List<String>,
    val options: List<String>,
    val wornOptions: List<String>,
    val edible: Boolean,
    val examine: String,
    val value: Int,
    val weight: Double,
    val canSellOnExchange: Boolean,
    val release: Date,
    val update: String,
    val removal: Date?,
    val removalUpdate: String?,
    val lastUpdate: Date,
    val equipmentItemInfo: EquipmentItemInfo?,
    val exchangeInfo: WikiExchangeData?,
    val isHistoricId: Boolean,
    val isBetaId: Boolean,
) {

    val highAlchValue: Int get() = if (alchable) (value * 0.6).toInt() else 0
    val lowAlchValue: Int get() = if (alchable) (value * 0.4).toInt() else 0
    val isEquipmentItem: Boolean get() = equipmentItemInfo != null && equippable

    fun debug() = debug("")
    fun debug(prefix: String) {
        fun prefixPrint(string: String) = println("$prefix$string")
        prefixPrint("Item: $name${if (version.isNotEmpty()) " ($version)" else ""} [$id]")
        prefixPrint("    Image: $image")
        if (aka.isNotEmpty()) prefixPrint("    AKA: $aka")
        prefixPrint("    Members: $members")
        prefixPrint("    Alchable: $alchable")
        prefixPrint("    Equippable: $equippable")
        prefixPrint("    Tradeable: $tradeable")
        prefixPrint("    Stackable: $stackable")
        prefixPrint("    Stacks in bank: $stacksInBank")
        prefixPrint("    Placeholder: $placeholder")
        prefixPrint("    Quest: $quest")
        prefixPrint("    Destroy: $destroy")
        prefixPrint("    Options: $options")
        if (wornOptions.isNotEmpty()) prefixPrint("    Worn options: $wornOptions")
        prefixPrint("    Edible: $edible")
        prefixPrint("    Examine: $examine")
        prefixPrint("    Value: $value")
        if (alchable) prefixPrint("    High alch value: $highAlchValue")
        if (alchable) prefixPrint("    Low alch value: $lowAlchValue")
        prefixPrint("    Weight: $weight")
        prefixPrint("    Can sell on exchange: $canSellOnExchange")
        prefixPrint("    Release: ${release.toWikiAlternateFormat()}")
        prefixPrint("    Update: $update")
        if (removal != null) prefixPrint("    Removal: ${removal.toWikiAlternateFormat()}")
        if (removal != null) prefixPrint("    Removal update: $removalUpdate")
        prefixPrint("    Last update: ${lastUpdate.toWikiAlternateFormat()}")
        if (isHistoricId) prefixPrint("    isHistoricId: true")
        if (isBetaId) prefixPrint("    isBetaId: true")
        equipmentItemInfo?.debug("$prefix    ")
        exchangeInfo?.debug("$prefix    ")
    }

    companion object {

        internal fun String.boolean(): Boolean = when (this.lowercase()) {
            "yes", "y", "true", "t", "1" -> true
            "no", "n", "false", "f", "0" -> false
            else -> startsWith("yes", true)
        }

        fun fromJsonElement(jsonObject: JsonObject): Pair<String, List<ItemDetails>> {
            val list = mutableListOf<ItemDetails>()
            val infoObject = jsonObject["info"]
            val info = if (infoObject.isJsonObject) jsonObject["info"].asJsonObject else return "" to emptyList()
            val bonusResult = jsonObject["bonuses"]
            val bonuses = if (bonusResult != null) {
                if (bonusResult.isJsonArray) {
                    val array = bonusResult.asJsonArray
                    if (array.isEmpty) JsonNull.INSTANCE
                    else array[0].asJsonObject
                } else if (bonusResult.isJsonObject) bonusResult.asJsonObject
                else JsonNull.INSTANCE
            } else JsonNull.INSTANCE
            val title = jsonObject["title"].asString
            val infoboxVersions = info.toVersionedMap()
            val bonusesVersions = if (!bonuses.isJsonNull) bonuses.asJsonObject.toVersionedMap() else null
            val exchangeInfoJson =
                jsonObject["exchangeData"]?.let { if (it.isJsonObject) it.asJsonObject else JsonNull.INSTANCE }
                    ?: JsonNull.INSTANCE
            val exchangeData = if (exchangeInfoJson.isJsonNull) "" else exchangeInfoJson.toString()
            val versionCount = infoboxVersions.versions
            for (i in 0 until versionCount) {
                val infobox = infoboxVersions.getVersion(i + 1).toMutableMap()
                if ((infobox["exchange"]?.boolean() ?: infobox["tradeable"]?.boolean()) == true) infobox["exchangeInfo"] =
                    exchangeData
                val bonus = bonusesVersions?.getVersion(i + 1) ?: emptyMap()
                val details = fromMap(infobox, bonus)
                list.add(details)
            }
            return title to list
        }


        fun fromMap(infoMap: Map<String, String>, bonusesMap: Map<String, String> = emptyMap()): ItemDetails {
            val equipment = if (bonusesMap.isNotEmpty()) EquipmentItemInfo.fromMap(bonusesMap) else null
            var isHistoric = false
            var isBeta = false
            var idString = infoMap["id"] ?: ""
            if (idString.contains("hist")) isHistoric = true
            if (idString.contains("beta")) isBeta = true
            idString = idString.replace("hist", "").replace("beta", "")
            val id = idString.toIntOrNull() ?: -1

            val name = infoMap["name"] ?: ""
            val version = infoMap["version"] ?: ""
            val image = infoMap["image"] ?: ""
            val aka = infoMap["aka"]?.split(",") ?: emptyList()
            val members = infoMap["members"]?.boolean() ?: false
            val alchable = infoMap["alchable"]?.boolean() ?: true
            val equippable = infoMap["equipable"]?.boolean() ?: false
            val tradeable = infoMap["tradeable"]?.boolean() ?: false
            val stackable = infoMap["stackable"]?.boolean() ?: false
            val stacksInBank = infoMap["stacksinbank"]?.boolean() ?: true
            val placeholder = infoMap["placeholder"]?.boolean() ?: false
            val quest = if (infoMap["quest"] == "No") emptyList() else infoMap["quest"]?.split(",") ?: emptyList()
            val destroy = infoMap["destroy"]?.split(",") ?: emptyList()
            val options = infoMap["options"]?.split(",") ?: emptyList()
            val wornOptions = infoMap["wornoptions"]?.split(",") ?: emptyList()
            val edible = infoMap["edible"]?.boolean() ?: false
            val examine = infoMap["examine"] ?: ""
            val value = infoMap["value"]?.toIntOrNull() ?: 0
            val weight = infoMap["weight"]?.toDoubleOrNull() ?: 0.0
            val canSellOnExchange = infoMap["exchange"]?.boolean() ?: tradeable
            val release = infoMap["release"]?.getDateNonNullable() ?: DefaultDate
            val update = infoMap["update"] ?: ""
            val removal = infoMap["removal"]?.getDateNullable()
            val removalUpdate = infoMap["removalupdate"] ?: ""
            val lastUpdate = infoMap["lastupdate"]?.getDateNullable() ?: release
            val exchangeInfoString = infoMap["exchangeInfo"] ?: ""

            val exchangeInfo = if (exchangeInfoString.isNotEmpty()) {
                val json = JsonParser.parseString(exchangeInfoString)
                GSON.fromJson(json, WikiExchangeData::class.java)
            } else null


            return ItemDetails(
                id, name, version, image,
                aka, members, alchable, equippable, tradeable,
                stackable, stacksInBank, placeholder, quest,
                destroy, options, wornOptions, edible,
                examine, value, weight, canSellOnExchange,
                release, update, removal, removalUpdate, lastUpdate,
                equipment, exchangeInfo, isHistoric, isBeta
            )

        }
    }
}