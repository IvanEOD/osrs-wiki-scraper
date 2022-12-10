package org.tribot.wikiscraper

import org.tribot.wikiscraper.query.getItemTemplates


/* Written by IvanEOD 12/9/2022, at 9:03 AM */

fun main() {

    val wiki = OsrsWiki.builder().build()

    val itemTemplates = wiki.getItemTemplates()


}