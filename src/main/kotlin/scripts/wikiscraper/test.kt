package scripts.wikiscraper

import scripts.wikiscraper.query.*
import scripts.wikiscraper.utility.GSON


/* Written by IvanEOD 12/9/2022, at 9:03 AM */

fun main() {

    val wiki = OsrsWiki.builder().build()

    val titles = wiki.getAllTitlesUsingTheseTemplates("LocLine", "Infobox Monster")
    println("Found ${titles.size} titles that use both LocLine and Infobox Monster templates")
    val results = wiki.getTemplatesFromTitles(listOf("LocLine", "Infobox Monster"), titles.subList(0, maxOf(20, titles.size)))
    println(results)
//
//    println(results)

}


//    runBlocking {
//
//        val titles: List<String>
//        val time = measureTimeMillis {
//            titles = wiki.getAllNpcTitles()
//        }
//        val monsters = titles.take(5)
//        println("Titles: $monsters")
//        val details = wiki.getNpcDetails(*monsters.toTypedArray())
//        details.forEach { (title, list) ->
//            list.forEach { println(it) }
//        }
//
//    }





//    val itemTemplates = wiki.getItemTemplates()
//
//    itemTemplates.forEach { (title, template) ->
//        if (template.id == -1) println("Couldn't find id for $title")
////        template.debug("")
//    }
