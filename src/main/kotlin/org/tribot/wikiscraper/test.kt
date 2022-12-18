package org.tribot.wikiscraper

import org.tribot.wikiscraper.query.getQuestRequirements


/* Written by IvanEOD 12/9/2022, at 9:03 AM */

fun main() {

    val wiki = OsrsWiki.builder().build()

    val results = wiki.getQuestRequirements()
    println(results)


//    val itemTemplates = wiki.getItemTemplates()
//
//    itemTemplates.forEach { (title, template) ->
//        if (template.id == -1) println("Couldn't find id for $title")
////        template.debug("")
//    }

}