package org.tribot.wikiscraper.utility


/* Written by IvanEOD 12/9/2022, at 8:44 AM */
data class WikiItemPrice(private val map: Map<String, Long>) {
    val high: Long by map
    val highTime: Long by map
    val low: Long by map
    val lowTime: Long by map
    override fun toString() = "High: $high [${highTime.toDate("HH:mm:ss")}], Low: $low [${lowTime.toDate("HH:mm:ss")}]"

    companion object {
        const val ItemPriceUrl = "https://prices.runescape.wiki/api/v1/osrs/latest"
    }

    data class WikiResponse(val data: Map<Int, Map<String, Long>>)
}

