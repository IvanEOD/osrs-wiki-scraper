package org.tribot.wikiscraper.classes

/* Written by IvanEOD 12/9/2022, at 8:44 AM */
data class EquipmentItemInfo(
    val slot: String,
    val isTwoHanded: Boolean,
    val combatStyle: String,
    val attackStats: GeneralStats = GeneralStats(),
    val defenceStats: GeneralStats = GeneralStats(),
    val otherStats: OtherStats = OtherStats(),
//    val image: String?,
//    val caption: String?,
//    val altImage: String?,
//    val altCaption: String?,
) {
    constructor(
        slot: String, twoHanded: Boolean, combatStyle: String,
        astab: Int, aslash: Int, acrush: Int, amagic: Int, arange: Int,
        dstab: Int, dslash: Int, dcrush: Int, dmagic: Int, drange: Int,
        str: Int, rstr: Int, mstr: Int, prayer: Int, attackRange: Int?, speed: Int?,
//        image: String, caption: String, altImage: String, altCaption: String
    ): this(slot, twoHanded, combatStyle,
        GeneralStats(astab, aslash, acrush, amagic, arange),
        GeneralStats(dstab, dslash, dcrush, dmagic, drange),
        OtherStats(str, rstr, mstr, prayer, attackRange, speed),
//        image, caption, altImage, altCaption
    )

    data class GeneralStats(
        val stab: Int = 0,
        val slash: Int = 0,
        val crush: Int = 0,
        val magic: Int = 0,
        val range: Int = 0,
    )
    data class OtherStats(
        val strength: Int = 0,
        val rangedStrength: Int = 0,
        val magicDamage: Int = 0,
        val prayer: Int = 0,
        val attackRange: Int? = null,
        val speed: Int? = null,
    )

    companion object {
        private fun getInt(string: String?): Int? {
            if (string.isNullOrEmpty()) return null
            var neg = false
            val trimmed = string.trim()
            if (trimmed.startsWith("-")) neg = true
            return trimmed.replace("-", "")
                .replace("+", "")
                .toIntOrNull()?.let { if (neg) -it else it }
        }


        fun fromMap(map: Map<String, String>): EquipmentItemInfo {
            var slot = map["slot"] ?: ""
            var twoHanded = false
            if (slot == "2h") {
                slot = "weapon"
                twoHanded = true
            }
            val attackStats = GeneralStats(
                getInt(map["astab"]) ?: 0,
                getInt(map["aslash"]) ?: 0,
                getInt(map["acrush"]) ?: 0,
                getInt(map["amagic"]) ?: 0,
                getInt(map["arange"]) ?: 0,
            )
            val defenceStats = GeneralStats(
                getInt(map["dstab"]) ?: 0,
                getInt(map["dslash"]) ?: 0,
                getInt(map["dcrush"]) ?: 0,
                getInt(map["dmagic"]) ?: 0,
                getInt(map["drange"]) ?: 0,
            )
            val otherStats = OtherStats(
                getInt(map["str"]) ?: 0,
                getInt(map["rstr"]) ?: 0,
                getInt(map["mstr"]) ?: 0,
                getInt(map["prayer"]) ?: 0,
                getInt(map["attackrange"]),
                getInt(map["speed"]),
            )

            return EquipmentItemInfo(
                slot,
                twoHanded,
                map["combatstyle"] ?: "",
                attackStats,
                defenceStats,
                otherStats,
//                map["image"],
//                map["caption"],
//                map["altimage"],
//                map["altcaption"],
            )
        }
    }

}