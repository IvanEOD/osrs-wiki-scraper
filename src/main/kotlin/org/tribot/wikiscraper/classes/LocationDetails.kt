package org.tribot.wikiscraper.classes

data class LocationCoordinates(
    val x: Int,
    val y: Int,
    val plane: Int
)

data class LocationDetails(
    val locationType: String,
    val mapId: Int,
    val geometryType: String,
    val tiles: List<LocationCoordinates>,
)
