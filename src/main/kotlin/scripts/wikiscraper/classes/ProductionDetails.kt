package scripts.wikiscraper.classes

import com.google.gson.annotations.SerializedName
import scripts.wikiscraper.classes.ItemDetails.Companion.boolean


/* Written by IvanEOD 12/20/2022, at 12:48 PM */

data class ProductionDetails(
    val ticks: Int,
    val notes: String,
    val members: Boolean,
    val requirements: List<ProductionRequirement>
) {
    fun hasSkillRequirements(): Boolean = requirements.any { it is ProductionRequirement.Skill }
    fun hasMaterialRequirements(): Boolean = requirements.any { it is ProductionRequirement.Material }
    fun hasFacilityRequirements(): Boolean = requirements.any { it is ProductionRequirement.Facility }
    fun getMaterialRequirements(): List<ProductionRequirement.Material> = requirements.filterIsInstance<ProductionRequirement.Material>()
    fun getSkillRequirements(): List<ProductionRequirement.Skill> = requirements.filterIsInstance<ProductionRequirement.Skill>()
    fun getFacilityRequirements(): List<ProductionRequirement.Facility> = requirements.filterIsInstance<ProductionRequirement.Facility>()
}

sealed class ProductionRequirement {
    data class Material(val name: String, val amount: Int) : ProductionRequirement()
    data class Skill(val name: String, val level: Int, val boostable: Boolean = false, val experienceYielded: Int) :
        ProductionRequirement()
    data class Tool(val name: String) : ProductionRequirement()
    data class Facility(val name: String) : ProductionRequirement()
}

data class ProductionOutput(
    @SerializedName("name") var name: String? = null,
    @SerializedName("image") var image: String? = null,
    @SerializedName("quantity") var quantity: String? = null,
    @SerializedName("txt") var text: String? = null,
    @SerializedName("cost") var cost: Int? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("outputnote") val itemNote: String? = null,
    @SerializedName("quantitynote") val quantityNote: String? = null,
    @SerializedName("subtxt") val subtext: String? = null
)

data class ProductionDetailsJson(
    @SerializedName("ticks") var ticks: String? = null,
    @SerializedName("notes") var notes: String? = null,
    @SerializedName("materials") var materials: ArrayList<Materials> = arrayListOf(),
    @SerializedName("tools") var tools: String? = null,
    @SerializedName("facilities") var facility: String? = null,
    @SerializedName("skills") var skills: ArrayList<Skills> = arrayListOf(),
    @SerializedName("members") var members: String? = null,
    @SerializedName("output") var output: ProductionOutput? = ProductionOutput()
) {

    fun toProductionDetails(): ProductionDetails {
        val skillRequirements = skills.map { it.toProductionRequirement() }
        val materialRequirements = materials.map { it.toProductionRequirement() }
        val tools = tools?.split(",")?.map { it.trim() }?.map { ProductionRequirement.Tool(it) } ?: emptyList()
        val facilities = facility?.split(",")?.map { it.trim() }?.map { ProductionRequirement.Facility(it) } ?: emptyList()
        return ProductionDetails(
            ticks = ticks?.toIntOrNull() ?: 0,
            notes = notes ?: "",
            members = members?.boolean() ?: false,
            requirements = skillRequirements + materialRequirements + tools + facilities
        )
    }

    data class Materials(
        @SerializedName("name") var name: String? = null,
        @SerializedName("quantity") var quantity: String? = null
    ) {
        fun toProductionRequirement(): ProductionRequirement.Material =
            ProductionRequirement.Material(name ?: "", quantity?.toIntOrNull() ?: 1)
    }

    data class Skills(
        @SerializedName("experience") var experience: String? = null,
        @SerializedName("level") var level: String? = null,
        @SerializedName("name") var name: String? = null,
        @SerializedName("boostable") var boostable: String? = null
    ) {
        fun toProductionRequirement(): ProductionRequirement = ProductionRequirement.Skill(
            name = name ?: "",
            level = level?.toIntOrNull() ?: 1,
            boostable = boostable?.boolean() ?: false,
            experienceYielded = experience?.toIntOrNull() ?: 0
        )
    }

}
