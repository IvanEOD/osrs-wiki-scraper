package scripts.wikiscraper.classes

import scripts.wikiscraper.utility.DefaultDate
import java.util.*

data class WikiExchangeData(
    var name: String = "",
    var link: String = "",
    var icon: String = "",
    var examine: String = "",
    var volume: Int = 0,
    var value: Int = 0,
    var price: Int = 0,
    var lowAlch: Int = 0,
    var last: Int = 0,
    var id: Int = 0,
    var highAlch: Int = 0,
    var buyLimit: Int = 0,
    var change: Double = 0.0,
    var lastDate: Date = DefaultDate,
    var date: Date = DefaultDate,
    var members: Boolean = false,
) {

    fun debug(prefix: String) {
        fun prefixPrint(string: String) = println("$prefix$string")
        prefixPrint("Exchange Data:")
        prefixPrint("    Exchange Name: $name")
        prefixPrint("    Exchange id: $id")
        prefixPrint("    Data Graph Link: $link")
        prefixPrint("    Price: $price")
        prefixPrint("    Last price: $last")
        prefixPrint("    Price change: $change")
        prefixPrint("    Price date: $date")
        prefixPrint("    Last price date: $lastDate")
        prefixPrint("    Buy limit: $buyLimit")
        prefixPrint("    Exchange volume: $volume")
        prefixPrint("    Members: $members")
        prefixPrint("    Icon: $icon")
        prefixPrint("    Examine: $examine")
        prefixPrint("    Value: $value")
        prefixPrint("    High alch: $highAlch")
        prefixPrint("    Low alch: $lowAlch")


    }


}