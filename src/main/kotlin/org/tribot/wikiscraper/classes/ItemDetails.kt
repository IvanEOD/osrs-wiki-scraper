package org.tribot.wikiscraper.classes

import org.tribot.wikiscraper.utility.DefaultDate
import org.tribot.wikiscraper.utility.getDateNonNullable
import org.tribot.wikiscraper.utility.getDateNullable
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
    val equipmentItemInfo: EquipmentItemInfo?,
    val isHistoricId: Boolean,
    val isBetaId: Boolean,
) {

    val highAlchValue: Int get() = if (alchable) (value * 0.6).toInt() else 0
    val lowAlchValue: Int get() = if (alchable) (value * 0.4).toInt() else 0

    val isEquipmentItem: Boolean get() = equipmentItemInfo != null && equippable


    companion object {

        private fun String.boolean(): Boolean = when (this.lowercase()) {
            "yes", "y", "true", "t", "1" -> true
            "no", "n", "false", "f", "0" -> false
            else -> startsWith("yes", true)
        }

        fun fromMap(map: Map<String, String>, itemDetailsMap: Map<String, String> = emptyMap()): ItemDetails {
            val equipment = if (itemDetailsMap.isNotEmpty()) EquipmentItemInfo.fromMap(itemDetailsMap) else null
            var isHistoric = false
            var isBeta = false
            var idString = map["id"] ?: ""
            if (idString.contains("hist")) isHistoric = true
            if (idString.contains("beta")) isBeta = true
            idString = idString.replace("hist", "").replace("beta", "")
            val id = idString.toIntOrNull() ?: -1

            val name = map["name"] ?: ""
            val version = map["version"] ?: ""
            val image = map["image"] ?: ""
            val aka = map["aka"]?.split(",") ?: emptyList()
            val members = map["members"]?.boolean() ?: false
            val alchable = map["alchable"]?.boolean() ?: true
            val equippable = map["equipable"]?.boolean() ?: false
            val tradeable = map["tradeable"]?.boolean() ?: false
            val stackable = map["stackable"]?.boolean() ?: false
            val stacksInBank = map["stacksinbank"]?.boolean() ?: true
            val placeholder = map["placeholder"]?.boolean() ?: false
            val quest = if (map["quest"] == "No") emptyList() else map["quest"]?.split(",") ?: emptyList()
            val destroy = map["destroy"]?.split(",") ?: emptyList()
            val options = map["options"]?.split(",") ?: emptyList()
            val wornOptions = map["wornoptions"]?.split(",") ?: emptyList()
            val edible = map["edible"]?.boolean() ?: false
            val examine = map["examine"] ?: ""
            val value = map["value"]?.toIntOrNull() ?: 0
            val weight = map["weight"]?.toDoubleOrNull() ?: 0.0
            val canSellOnExchange = map["exchange"]?.boolean() ?: tradeable
            val release = map["release"]?.getDateNonNullable(DefaultDate) ?: DefaultDate
            val update = map["update"] ?: ""
            val removal = map["removal"]?.getDateNullable()
            val removalUpdate = map["removalupdate"] ?: ""

            return ItemDetails(
                id, name, version, image,
                aka, members, alchable, equippable, tradeable,
                stackable, stacksInBank, placeholder, quest,
                destroy, options, wornOptions, edible,
                examine, value, weight, canSellOnExchange,
                release, update, removal, removalUpdate,
                equipment, isHistoric, isBeta
            )

        }
    }
}