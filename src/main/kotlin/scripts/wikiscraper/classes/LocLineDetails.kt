package scripts.wikiscraper.classes

import com.google.gson.JsonObject
import scripts.wikiscraper.classes.ItemDetails.Companion.boolean


/* Written by IvanEOD 12/20/2022, at 5:23 PM */
data class LocLineDetails(
    val name: String,
    val levels: List<Int>,
    val mtype: String,
    val location: String,
    val mapID: Int,
    val members: Boolean,
    val coordinates: List<Coordinate>
) {

    data class Coordinate(
        val x: Int,
        val y: Int,
        val plane: Int = 0,
        val id: Int? = null,
    ) {

        override fun toString(): String = "($x, $y, $plane${if (id != null) ", [id:$id]" else ""})"

        companion object {
            fun fromString(plane: Int, value: String): Coordinate {
                var x: Int = -1
                var y: Int = -1
                var id: Int? = null
                val keyValuePairs = value.split(",")
                if (keyValuePairs.size == 2
                    && keyValuePairs.none { it.contains(":") }
                    && keyValuePairs.mapNotNull { it.toIntOrNull() }.size == 2
                ) {
                    x = keyValuePairs[0].toInt()
                    y = keyValuePairs[1].toInt()
                    return Coordinate(x, y, plane, id)
                } else {
                    for (keyValuePair in keyValuePairs) {
                        val split = keyValuePair.trim().split(":")
                        val key = split.getOrNull(0) ?: continue
                        val v = split.getOrNull(1) ?: ""
                        if (key.equals("npcid", true) || key.equals("title", true)) {
                            val intValue = v.toIntOrNull()
                            if (intValue != null) id = intValue
                        }
                        if (key == "x") x = v.toInt()
                        if (key == "y") y = v.toInt()
                    }
                    if (y == -1 || x == -1) println("Error parsing coordinate: $value")
                    return Coordinate(x, y, plane, id)
                }
            }
        }
    }

    fun debug(prefix: String = "") {
        fun prefixPrint(message: String) { println("$prefix$message") }
        prefixPrint("Name: $name")
        prefixPrint("    Levels: $levels")
        prefixPrint("    Type: $mtype")
        prefixPrint("    Location: $location")
        prefixPrint("    Map ID: $mapID")
        prefixPrint("    Members: $members")
        prefixPrint("    Coordinates: ${if (coordinates.isEmpty()) "[]" else ""}")
        for (coordinate in coordinates) {
            prefixPrint("        $coordinate")
        }
    }

    companion object {
        fun fromJsonObject(jsonObject: JsonObject): LocLineDetails {
            val coordinates = mutableListOf<Coordinate>()
            val plane = jsonObject["plane"]?.asString?.toIntOrNull() ?: 0
            var index = -1
            while (true) {
                index++
                val coordinate = jsonObject["$index"]?.asString?.let { Coordinate.fromString(plane, it) }
                    ?: if (index == 0) continue else break
                coordinates.add(coordinate)
            }
            return LocLineDetails(
                jsonObject["name"]?.asString ?: "",
                jsonObject["levels"].asString.split(",").mapNotNull { it.toIntOrNull() },
                jsonObject["mtype"]?.asString ?: "unknown",
                jsonObject["location"].asString,
                jsonObject["mapID"]?.asString?.toIntOrNull() ?: 0,
                jsonObject["members"]?.asString?.boolean() ?: false,
                coordinates
            )

        }
    }


}