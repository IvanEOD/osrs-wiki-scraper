package scripts.wikiscraper.classes

/* Written by IvanEOD 12/9/2022, at 8:44 AM */
sealed class QuestRequirement {
    data class Quest(val name: String): QuestRequirement()
    data class QuestPoint(val amount: Int): QuestRequirement()
    data class CombatLevel(val level: Int): QuestRequirement()
    data class Skill(val name: String, val level: Int, val boostable: Boolean = false, val ironmanConcern: Boolean = false): QuestRequirement()
    data class Favor(val name: String, val percent: Int): QuestRequirement()
    data class Kudos(val amount: Int): QuestRequirement()
    data class BarbarianAssault(val role: String, val level: Int): QuestRequirement()

    override fun toString(): String = when(this) {
        is Quest -> "Quest($name)"
        is CombatLevel -> "Combat Level($level)"
        is QuestPoint -> "Quest Points($amount)"
        is Skill -> "Skill($name, Level: $level, Boostable: $boostable, Ironman Concern: $ironmanConcern)"
        is Favor -> "Favor($name, $percent%)"
        is Kudos -> "Kudos($amount)"
        is BarbarianAssault -> "Barbarian Assault($role, Level: $level)"
    }

}
