package org.tribot.wikiscraper.classes

data class LocationDetails(
    val name: String,
    val type: String,
    val mapId: Int,
    val tiles: List<List<Int>>,
)

data class Geometry (
    val type: String,
    val coordinates: List<List<List<Long>>>
)

data class Properties (
    val mapID: Long,
    val plane: Long
)