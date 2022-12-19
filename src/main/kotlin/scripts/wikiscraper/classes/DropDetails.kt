package scripts.wikiscraper.classes

import com.google.gson.annotations.SerializedName


data class DropDetails(

    @SerializedName("Name Notes") var nameNotes: String = "",
    @SerializedName("Rarity Notes") var rarityNotes: String = "",
    @SerializedName("Rarity") var rarity: String = "",
    @SerializedName("Alt Rarity Dash") var altRarityDash: String = "",
    @SerializedName("Drop level") var dropLevel: String = "",
    @SerializedName("Drop Quantity") var dropQuantity: String = "",
    @SerializedName("Drop type") var dropType: String = "",
    @SerializedName("Dropped from") var droppedFrom: String = "",
    @SerializedName("Quantity High") var quantityHigh: Int = 0,
    @SerializedName("Drop Value") var dropValue: Int = 0,
    @SerializedName("Rolls") var rolls: Int = 0,
    @SerializedName("Alt Rarity") var altRarity: String = "",
    @SerializedName("Quantity Low") var quantityLow: Int = 0,
    @SerializedName("Dropped item") var droppedItem: String = ""

)