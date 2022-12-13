package org.tribot.wikiscraper.lua

import org.tribot.wikiscraper.utility.pipeFence


/* Written by IvanEOD 12/12/2022, at 2:26 PM */

fun ScribuntoSession.ask(query: Map<String, Any>) {
    val response = request {
        "query" `=` query.local()
        +"dplAsk(query)"
    }
    println(response)
}

fun ScribuntoSession.getTemplatesOnPage(title: String): List<String> {
    val list = mutableListOf<String>()

    val response = request {
        +"getTemplatesOnPage('$title')"
    }

    println(response)

    return list
}
fun ScribuntoSession.getPagesInCategory(vararg category: String): List<String> {
    val list = mutableListOf<String>()

    when (category.size) {
        0 -> return list
        1 -> {
            val response = request {
                +"getPagesInCategories({ '${category[0]}' })"
            }
            println(response)
        }
        else -> {
            val response = request {
                "categories" `=` category.toList().local()
                +"getPagesInCategories(categories)"
            }
            println(response)
        }
    }

    return list
}