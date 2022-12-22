package scripts.wikiscraper.examples

import scripts.wikiscraper.OsrsWiki
import scripts.wikiscraper.query.getQuestRequirements


/* Written by IvanEOD 12/22/2022, at 12:23 AM */

fun main() {

    val wiki = OsrsWiki.builder().build()

    val questData = wiki.getQuestRequirements()

    // Don't want to print them all...
    val chunk = questData.toList().take(5).toMap()

    chunk.forEach { (title, requirements) ->
        println("Quest: $title")
        requirements.map { "    $it" }.forEach(::println)
    }

}

/*

Quest: Fairytale I - Growing Pains
    Quest(name=Lost City)
    Quest(name=Nature Spirit)
Quest: Enlightened Journey
    QuestPoint(amount=20)
    Skill(name=Firemaking, level=20, boostable=true, ironmanConcern=false)
    Skill(name=Farming, level=30, boostable=true, ironmanConcern=false)
    Skill(name=Crafting, level=36, boostable=true, ironmanConcern=false)
Quest: The Ascent of Arceuus
    Quest(name=Client of Kourend)
    Skill(name=Hunter, level=12, boostable=false, ironmanConcern=false)
    Favor(name=Arceuus, percent=20)
Quest: Elemental Workshop II
    Quest(name=Elemental Workshop I)
    Skill(name=Magic, level=20, boostable=true, ironmanConcern=false)
    Skill(name=Smithing, level=30, boostable=true, ironmanConcern=false)
Quest: Architectural Alliance
    Skill(name=Mining, level=42, boostable=true, ironmanConcern=false)
    Favor(name=Arceuus, percent=100)
    Favor(name=Hosidius, percent=100)
    Favor(name=Lovakengj, percent=100)
    Favor(name=Piscarilius, percent=100)
    Favor(name=Shayzien, percent=100)

 */