package org.tribot.wikiscraper.classes

import org.tribot.wikiscraper.utility.getDateNullable
import org.tribot.wikiscraper.utility.getDateRangeNullable
import java.util.*


/* Written by IvanEOD 12/9/2022, at 3:46 PM */
data class SubjectChanges(
    val date: Date?,
    val dateRange: ClosedRange<Date>?,
    val update: String?,
    val poll: String?,
    val type: String?,
    val change: String,
) {
    companion object {
        fun fromMap(map: Map<String, String>): SubjectChanges = SubjectChanges(
            map["date"]?.getDateNullable(),
            map["daterange"]?.getDateRangeNullable(),
            map["update"],
            map["poll"],
            map["type"],
            map["change"] ?: "",
        )
    }
}