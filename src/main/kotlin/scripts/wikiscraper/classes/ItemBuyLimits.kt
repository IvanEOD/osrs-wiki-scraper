package scripts.wikiscraper.classes

/* Written by IvanEOD 12/9/2022, at 8:44 AM */
data class ItemBuyLimits(
    val lastWikiUpdate: Long,
    val lastWikiUpdateString: String,
    val items: Map<String, Int>
) {
    operator fun get(itemName: String): Int? = items[itemName]

    override fun toString(): String = "ItemBuyLimits(" +
            "lastWikiUpdate=$lastWikiUpdate, " +
            "lastWikiUpdateString='$lastWikiUpdateString', " +
            "items=$items)"

    companion object {
        const val BuyLimitsUrl = "https://oldschool.runescape.wiki/w/Module:GELimits/data?action=raw"
        val BuyLimitsRegex = "\\[\"(.*?)\"]\\s=\\s(.*?),".toRegex()
    }
}