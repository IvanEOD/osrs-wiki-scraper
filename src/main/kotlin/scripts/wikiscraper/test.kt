package scripts.wikiscraper


import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import scripts.wikiscraper.classes.ItemDetails
import scripts.wikiscraper.query.*
import kotlin.system.measureTimeMillis


/* Written by IvanEOD 12/9/2022, at 9:03 AM */

fun main() {

    val wiki = OsrsWiki.builder().build()

//    val results = wiki.getQuestRequirements()
//    println(results)
    runBlocking {

        val titles: List<String>
        val time = measureTimeMillis {
            titles = wiki.getAllNpcTitles()
        }
        val monsters = titles.take(5)
        println("Titles: $monsters")
        val details = wiki.getNpcDetails(*monsters.toTypedArray())
        details.forEach { (title, list) ->
            list.forEach { println(it) }
        }

    }





//    val itemTemplates = wiki.getItemTemplates()
//
//    itemTemplates.forEach { (title, template) ->
//        if (template.id == -1) println("Couldn't find id for $title")
////        template.debug("")
//    }

}