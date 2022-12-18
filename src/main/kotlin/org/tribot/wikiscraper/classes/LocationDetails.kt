package org.tribot.wikiscraper.classes

import com.google.gson.JsonElement
import org.tribot.wikiscraper.utility.GSON

data class Coordinates(
    val x: Int,
    val y: Int,
    val plane: Int
)

data class LocationDetails(
    val locationType: String,
    val mapId: Int,
    val geometryType: String,
    val tiles: List<Coordinates>,
) {

    companion object {
        fun fromJsonElement(element: JsonElement): LocationDetails {
            val obj = element.asJsonObject
            val locationType = obj["type"].asString
            val geometryObject = obj["geometry"].asJsonObject
            val geometryType = geometryObject["type"].asString
            val coordinates = geometryObject["coordinates"].asJsonArray
            val properties = obj["properties"].asJsonObject
            val mapId = properties["mapID"].asInt
            val plane = properties["plane"].asInt
            val locationCoordinatesList = mutableListOf<Coordinates>()

            for (coordinate in coordinates) {
                val coordinateList = GSON.fromJson(coordinate, Array<Array<Int>>::class.java)
                for (coordinatePair in coordinateList) {
                    val x = coordinatePair[0]
                    val y = coordinatePair[1]
                    val location = Coordinates(x, y, plane)
                    locationCoordinatesList.add(location)
                }
            }
            return LocationDetails(locationType, mapId, geometryType, locationCoordinatesList)
        }
    }

}
