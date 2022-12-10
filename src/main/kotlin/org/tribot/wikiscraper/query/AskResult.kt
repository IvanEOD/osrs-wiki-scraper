package org.tribot.wikiscraper.query

/* Written by IvanEOD 12/9/2022, at 8:19 AM */
data class AskResult(
    val title: String,
    val printouts: Map<String, String>,
    val fulltext: String,
    val fullurl: String,
    val namespace: Int,
    val exists: Boolean,
)