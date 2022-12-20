package scripts.wikiscraper.classes

import com.google.gson.JsonObject
import scripts.wikiscraper.classes.ItemDetails.Companion.boolean
import scripts.wikiscraper.utility.DefaultDate
import scripts.wikiscraper.utility.getDateNonNullable
import scripts.wikiscraper.utility.getDateNullable
import scripts.wikiscraper.utility.toVersionedMap
import java.util.*

data class NpcDetails(
    val id: List<Int> = emptyList(),
    val name: String,
    val version: String? = null,
    val image: String? = null,
    val aka: List<String> = emptyList(),
    val members: Boolean = false,
    val release: Date = DefaultDate,
    val update: String = "",
    val removal: Date? = null,
    val removalUpdate: String? = null,
    val lastUpdate: Date = release,
    val examine: String = "",
    val level: Int? = null,
    val race: String? = null,
    val quest: String? = null,
    val location: String? = null,
    val shop: String? = null,
    val gender: String? = null,
    val options: List<String> = emptyList(),
    val npcMap: String? = null,
) {

    companion object {

        fun fromJsonObject(jsonObject: JsonObject): Map<String, List<NpcDetails>> {
            val versionedMap = jsonObject.toVersionedMap()
            val map = mutableMapOf<String, MutableList<NpcDetails>>()
            versionedMap.getIndividualVersions().forEach {
                val details = fromMap(it)
                map.getOrPut(details.name) { mutableListOf() }.add(details)
            }
            return map
        }

        fun fromMap(map: Map<String, String>): NpcDetails {

            val idsString = map["id"] ?: ""
            val idsStringList = idsString.split(",")
            val cleanedIds = idsStringList.map {
                it.replace("hist", "").replace("beta", "")
            }
            val id = cleanedIds.mapNotNull { it.toIntOrNull() }
            val name = map["name"] ?: ""
            val version = map["version"] ?: ""
            val image = map["image"] ?: ""
            val aka = map["aka"]?.split(",") ?: emptyList()
            val members = map["members"]?.boolean() ?: false
            val release = map["release"]?.getDateNonNullable() ?: DefaultDate
            val update = map["update"] ?: ""
            val removal = map["removal"]?.getDateNullable()
            val removalUpdate = map["removalupdate"] ?: ""
            val lastUpdate = map["lastupdate"]?.getDateNullable() ?: release
            val examine = map["examine"] ?: ""
            val level = map["combat"]?.toIntOrNull()
            val race = map["race"]
            val quest = map["quest"]
            val location = map["location"]
            val shop = map["shop"]
            val gender = map["gender"]
            val options = map["options"]?.split(",") ?: emptyList()
            val npcMap = map["map"]?.let {
                if (it == "No" || it == "no") ""
                else it
            } ?: ""

            return NpcDetails(
                id = id,
                name = name,
                version = version,
                image = image,
                aka = aka,
                members = members,
                release = release,
                update = update,
                removal = removal,
                removalUpdate = removalUpdate,
                lastUpdate = lastUpdate,
                examine = examine,
                level = level,
                race = race,
                quest = quest,
                location = location,
                shop = shop,
                gender = gender,
                options = options,
                npcMap = npcMap,
            )

        }

    }

}