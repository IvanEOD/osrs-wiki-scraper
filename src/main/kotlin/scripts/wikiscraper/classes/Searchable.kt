package scripts.wikiscraper.classes

import me.xdrop.fuzzywuzzy.FuzzySearch
import scripts.wikiscraper.utility.GSON


/* Written by IvanEOD 1/3/2023, at 11:26 AM */
interface Searchable {

    val searchName: String
    val searchType: String get() = this::class.simpleName!!
    val searchContent: String
    val searchDescription: String
    val searchFullText: String get() = GSON.toJson(this)
    val searchTags: List<String>

    fun filter(criteria: SearchCriteria, tolerance: Int): Boolean = criteria.filter(this, tolerance)
    fun score(criteria: SearchCriteria): Int = criteria.score(this)

}

sealed class SearchCriteria(val value: String, val priority: Int) {
    class Generic(value: String): SearchCriteria(value, 10)
    class Name(value: String): SearchCriteria(value, 1)
    class Content(value: String): SearchCriteria(value, 3)
    class Description(value: String): SearchCriteria(value, 4)
    class FullText(value: String): SearchCriteria(value, 5)
    class ObjectType(value: String): SearchCriteria(value, 2)

    fun extractSearchString(searchable: Searchable) = when (this) {
        is Generic -> searchable.searchFullText + searchable.searchTags.joinToString { " " }
        is Name -> searchable.searchName
        is Content -> searchable.searchContent
        is Description -> searchable.searchDescription
        is FullText -> searchable.searchFullText
        is ObjectType -> searchable.searchType
    }

    fun score(searchable: Searchable) = score(extractSearchString(searchable)).also { println("$this : score = $it") }
    fun filter(searchable: Searchable, tolerance: Int) = score(searchable) >= tolerance

    fun score(text: String): Int {
        val cleaned = text.replace('_', ' ').replace('-', ' ').replace("\\s{2,}".toRegex(), " ")
        val score = FuzzySearch.weightedRatio(value, cleaned)
        println("$score : $value | $cleaned")
        return score
    }
    fun filter(text: String, tolerance: Int) = score(text) >= tolerance

    override fun toString(): String = "(${this.javaClass.simpleName.lowercase()}: $value)"
}

data class SearchData<T : Searchable>(val value: T) {
    val name: String = value.searchName
    val type: String = value.searchType
    val content: String = value.searchContent
    val description: String = value.searchDescription
    val fullText: String = value.searchFullText
}

fun List<SearchCriteria>.sort(): List<SearchCriteria> = this.sortedBy { it.priority }

fun Search(
    inputData: List<Searchable>,
    criteria: List<SearchCriteria>
): List<Searchable> {
    val sortedCriteria = criteria.sort()
    val results = mutableListOf<Searchable>()

    println("Criteria:")
    sortedCriteria.forEach { println("    $it") }

    println("Results found: ${inputData.size}")

    inputData.forEach { searchable ->
        var score = 0
        sortedCriteria.forEach { criteria ->
            score += criteria.score(searchable)
        }
        if (score > 0) {
            results.add(searchable)
        }
    }



    return results
}

