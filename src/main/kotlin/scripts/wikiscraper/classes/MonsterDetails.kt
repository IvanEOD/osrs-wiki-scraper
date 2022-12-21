package scripts.wikiscraper.classes

import com.google.gson.JsonObject
import scripts.wikiscraper.classes.ItemDetails.Companion.boolean
import scripts.wikiscraper.utility.DefaultDate
import scripts.wikiscraper.utility.getDateNonNullable
import scripts.wikiscraper.utility.getDateNullable
import scripts.wikiscraper.utility.toVersionedMap
import java.util.*

data class MonsterDetails(
    val id: List<Int>,
    val name: String = "",
    val version: String = "",
    val image: String = "",
    val aka: List<String> = emptyList(),
    val members: Boolean = false,
    val release: Date = DefaultDate,
    val update: String = "",
    val removal: Date? = null,
    val removalUpdate: String? = null,
    val lastUpdate: Date = release,
    val level: Int? = null,
    val size: Int = 1,
    val examine: String = "",
    val attributes: List<String> = emptyList(),
    val xpBonus: Double = 0.0,
    val maxHit: List<String> = emptyList(),
    val aggressive: Boolean = false,
    val poisonous: Boolean = false,
    val attackStyle: List<String> = emptyList(),
    val attackSpeed: Int = 0,
    val respawn: Int? = null,
    val slayerLevel: Int? = null,
    val slayerXp: Int? = null,
    val slayerCategory: String? = null,
    val assignedBy: List<String> = emptyList(),
    val hitpointsLevel: Int = 0,
    val attackLevel: Int = 0,
    val strengthLevel: Int = 0,
    val defenceLevel: Int = 0,
    val rangedLevel: Int = 0,
    val mageLevel: Int = 0,
    val attackBonus: Int = 0,
    val strengthBonus: Int = 0,
    val magicAttackBonus: Int = 0,
    val magicStrengthBonus: Int = 0,
    val rangeAttackBonus: Int = 0,
    val rangeStrengthBonus: Int = 0,
    val stabDefenceBonus: Int = 0,
    val slashDefenceBonus: Int = 0,
    val crushDefenceBonus: Int = 0,
    val magicDefenceBonus: Int = 0,
    val rangeDefenceBonus: Int = 0,
    val dropVersion: String = "",
    val immunePoison: Boolean = false,
    val immuneVenom: Boolean = false,
    val immuneCannon: Boolean = false,
    val immuneThrall: Boolean = false,

    ) {

    fun debug() {
        debug("")
    }

    fun debug(prefix: String) {
        fun prefixPrint(string: String) = println("$prefix$string")
        prefixPrint("Monster: $name${if (version.isNotEmpty()) " ($version)" else ""} [$id]")
        prefixPrint("    Image: $image")
        if (aka.isNotEmpty()) prefixPrint("    AKA: $aka")
        prefixPrint("    Members: $members")
        prefixPrint("    Level: $level")
        prefixPrint("    Size: $size")
        prefixPrint("    Examine: $examine")
        if (attributes.isNotEmpty()) prefixPrint("    Attributes: $attributes")
        prefixPrint("    XP Bonus: $xpBonus")
        if (maxHit.isNotEmpty()) prefixPrint("    Max Hit: $maxHit")
        prefixPrint("    Aggressive: $aggressive")
        prefixPrint("    Poisonous: $poisonous")
        if (attackStyle.isNotEmpty()) prefixPrint("    Attack Style: $attackStyle")
        prefixPrint("    Attack Speed: $attackSpeed")
        prefixPrint("    Respawn: $respawn")
        prefixPrint("    Slayer Level: $slayerLevel")
        prefixPrint("    Slayer XP: $slayerXp")
        if (slayerCategory != null) prefixPrint("    Slayer Category: $slayerCategory")
        if (assignedBy.isNotEmpty()) prefixPrint("    Assigned By: $assignedBy")
        prefixPrint("    Hitpoints Level: $hitpointsLevel")
        prefixPrint("    Attack Level: $attackLevel")
        prefixPrint("    Strength Level: $strengthLevel")
        prefixPrint("    Defence Level: $defenceLevel")
        prefixPrint("    Ranged Level: $rangedLevel")
        prefixPrint("    Mage Level: $mageLevel")
        prefixPrint("    Attack Bonus: $attackBonus")
        prefixPrint("    Strength Bonus: $strengthBonus")
        prefixPrint("    Magic Attack Bonus: $magicAttackBonus")
        prefixPrint("    Magic Strength Bonus: $magicStrengthBonus")
        prefixPrint("    Range Attack Bonus: $rangeAttackBonus")
        prefixPrint("    Range Strength Bonus: $rangeStrengthBonus")
        prefixPrint("    Stab Defence Bonus: $stabDefenceBonus")
        prefixPrint("    Slash Defence Bonus: $slashDefenceBonus")
        prefixPrint("    Crush Defence Bonus: $crushDefenceBonus")
        prefixPrint("    Magic Defence Bonus: $magicDefenceBonus")
        prefixPrint("    Range Defence Bonus: $rangeDefenceBonus")
        prefixPrint("    Drop Version: $dropVersion")
        prefixPrint("    Immune Poison: $immunePoison")
        prefixPrint("    Immune Venom: $immuneVenom")
        prefixPrint("    Immune Cannon: $immuneCannon")
        prefixPrint("    Immune Thrall: $immuneThrall")
        prefixPrint("    Release: $release")
        prefixPrint("    Update: $update")
        if (removal != null) prefixPrint("    Removal: $removal")
        if (removalUpdate != null) prefixPrint("    Removal Update: $removalUpdate")
        prefixPrint("    Last Update: $lastUpdate")


    }

    companion object {
        private fun isImmune(value: String?) = when (value) {
            "Immune" -> true
            "Not immune" -> false
            else -> value?.boolean() ?: false
        }

        fun fromJsonObject(jsonObject: JsonObject): Map<String, List<MonsterDetails>> {
            val versionedMap = jsonObject.toVersionedMap()
            println("Versioned Map: ")
            versionedMap.debug("")
            println("Map Contains Release: ${versionedMap.contains("release")}")
            val versions = versionedMap.versions
            val name = jsonObject["name"].asString
            println("Monster: $name, Versions: $versions")
            val map = mutableMapOf<String, MutableList<MonsterDetails>>()

            versionedMap.getIndividualVersions().forEach { versionMap ->
                val title = versionMap["name"]!!
                val details = fromMap(versionMap)
                map.getOrPut(title) { mutableListOf() }.add(details)
            }
            return map
        }

        fun fromMap(map: Map<String, String>): MonsterDetails {
            println(map)
            var isHistoric = false
            var isBeta = false
            val idsString = map["id"] ?: ""
            val idsStringList = idsString.split(",")
            val cleanedIds = idsStringList.map {
                if (it.contains("hist")) isHistoric = true
                if (it.contains("beta")) isBeta = true
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
            val level = map["combat"]?.toIntOrNull()
            val size = map["size"]?.toIntOrNull() ?: 1
            val examine = map["examine"] ?: ""
            val attributes: List<String> = map["attributes"]?.split(",") ?: emptyList()
            val xpBonus = map["xpbonus"]?.toDoubleOrNull() ?: 0.0
            val maxHit: List<String> = map["max hit"]?.split(",") ?: emptyList()
            val aggressive = map["aggressive"]?.boolean() ?: false
            val poisonous = map["poisonous"]?.boolean() ?: false
            val attackStyle = map["attack style"]?.split(",") ?: emptyList()
            val attackSpeed = map["attack speed"]?.toIntOrNull() ?: 0
            val respawn = map["respawn"]?.toIntOrNull()
            val slayerLevel = map["slaylvl"]?.toIntOrNull()
            val slayerXp = map["slayxp"]?.toIntOrNull()
            val slayerCategory = map["cat"]
            val assignedBy = map["assignedby"]?.split(",") ?: emptyList()
            val hitpoints = map["hitpoints"]?.toIntOrNull() ?: 0
            val attack = map["att"]?.toIntOrNull() ?: 0
            val strength = map["str"]?.toIntOrNull() ?: 0
            val defence = map["def"]?.toIntOrNull() ?: 0
            val ranged = map["range"]?.toIntOrNull() ?: 0
            val mage = map["mage"]?.toIntOrNull() ?: 0
            val attackBonus = map["attbns"]?.toIntOrNull() ?: 0
            val strengthBonus = map["strbns"]?.toIntOrNull() ?: 0
            val magicAttackBonus = map["amagic"]?.toIntOrNull() ?: 0
            val magicStrengthBonus = map["mbns"]?.toIntOrNull() ?: 0
            val rangeAttackBonus = map["arange"]?.toIntOrNull() ?: 0
            val rangeStrengthBonus = map["rngbns"]?.toIntOrNull() ?: 0
            val stabDefenceBonus = map["dstab"]?.toIntOrNull() ?: 0
            val slashDefenceBonus = map["dslash"]?.toIntOrNull() ?: 0
            val crushDefenceBonus = map["dcrush"]?.toIntOrNull() ?: 0
            val magicDefenceBonus = map["dmagic"]?.toIntOrNull() ?: 0
            val rangeDefenceBonus = map["drange"]?.toIntOrNull() ?: 0
            val dropVersion = map["dropversion"] ?: ""
            val immunePoison = isImmune(map["immunepoison"])
            val immuneVenom = isImmune(map["immunevenom"])
            val immuneCannon = isImmune(map["immunecannon"])
            val immuneThrall = isImmune(map["immunethrall"])

            return MonsterDetails(
                id, name, version, image, aka, members, release, update,
                removal, removalUpdate, lastUpdate, level, size, examine,
                attributes, xpBonus, maxHit, aggressive, poisonous, attackStyle,
                attackSpeed, respawn, slayerLevel, slayerXp, slayerCategory, assignedBy,
                hitpoints, attack, strength, defence, ranged, mage, attackBonus, strengthBonus,
                magicAttackBonus, magicStrengthBonus, rangeAttackBonus, rangeStrengthBonus,
                stabDefenceBonus, slashDefenceBonus, crushDefenceBonus, magicDefenceBonus,
                rangeDefenceBonus, dropVersion, immunePoison, immuneVenom, immuneCannon, immuneThrall,
            )
        }

    }

}
