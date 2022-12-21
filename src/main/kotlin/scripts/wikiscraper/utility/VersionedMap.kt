package scripts.wikiscraper.utility

/* Written by IvanEOD 12/4/2022, at 12:38 PM */
class VersionedMap(val versions: Int, internal val properties: List<TemplatePropertyData>) {
    constructor(map: Map<String, String>) : this(1, map.map { TemplatePropertyData(it.key, it.key, true, 0, it.value) })

    val keys: Set<String> get() = properties.map { it.name }.distinct().toSet()
    val wikiKeys: List<String> get() = properties.filter { it.isWikiKey }.map { it.key }.distinct()

    internal fun debug(prefix: String) {
        properties.groupBy { it.name }.forEach { (name, properties) ->
            if (properties.size > 1) {
                println("${prefix}Property: $name = {")
                properties.forEach {
                    println("$prefix    [${it.version}] = ${it.debugValueString()}")
                }
                println("$prefix}")
            }
            else {
                println("${prefix}Property: $name = ${properties[0].debugValueString()}")
            }
        }
    }

    val combined: Map<String, String>
        get() {
            val map = mutableMapOf<String, String>()
            properties.filter { it.version == 0 }.forEach { map[it.key] = it.cleanValue }
            return map
        }

    fun getVersion(version: Int = 0): Map<String, String> {
        if (version == 0) return combined
        if (versions == 1) return combined
        if (version > versions) {
            println("Version $version does not exist, returning version 1")
            debug("")
            return emptyMap()
//            throw IllegalArgumentException("Version $version does not exist, max version is $versions")
        }
        val map = mutableMapOf<String, String>()
        keys.forEach { key ->
            map[key] = this[key, version]
        }
        return map
    }

    fun getIndividualVersions(): List<Map<String, String>> = (1..versions).map { getVersion(it) }

    operator fun get(key: String, version: Int = 0): String {
        val finalVersion = if (version > versions) 1 else version
        val properties = this.properties.filter { it.name == key }
        if (properties.isEmpty()) {
            val byKey = properties.firstOrNull { it.key == key } ?: return ""
            return byKey.cleanValue
        }
        return if (version == 0) {
            if (properties.size == 1) properties.first().cleanValue
            else properties.joinToString(",") { it.cleanValue }
        } else {
            if (properties.size == 1) properties.first().cleanValue
            else properties.firstOrNull { it.version == version }?.cleanValue ?: ""
        }
    }

    operator fun contains(key: String): Boolean = properties.any { it.name == key }

}


data class TemplatePropertyData(
    val name: String,
    val key: String,
    val isWikiKey: Boolean,
    val version: Int,
    val value: String
) {
    override fun toString(): String = "$name ($key) = $value"
    var images = mutableListOf<String>()
    var files = mutableListOf<String>()
    private val pageReferencesList = mutableListOf<String>()
    val pageReferences: List<String> get() = pageReferencesList.distinct()
//    val pageReferences get() = if (pageReferencesList.isEmpty()) {
//        parsePageReferences()
//        pageReferencesList
//    } else pageReferencesList

    internal val cleanValue: String get() = if (isImage) "https://oldschool.runescape.wiki/images/${images[0].replace(' ', '_').urlEncode()}"
//        else if (isDate) WikiAlternateDateFormatter.format(WikiDoubleBracketDateFormatter.parse(value))
        else value
    private var remaining: String = ""
    private fun isOnlyOneItem(): Boolean = (images + files + pageReferences).size == 1
    val hasText: Boolean get() = remaining.isNotEmpty()
    val hasImage: Boolean get() = images.isNotEmpty()
    val hasFile: Boolean get() = files.isNotEmpty()
    val hasPageReference: Boolean get() = pageReferences.isNotEmpty()

    var isDate: Boolean = false
        private set
    val isImage: Boolean get() = !hasText && hasImage && isOnlyOneItem()
    val isFile: Boolean get() = !hasText && hasFile && isOnlyOneItem()
    val isPageReference: Boolean get() = (!hasText || value == "[[$remaining]]") && hasPageReference && isOnlyOneItem()

    init {
        parsePageReferences()
    }

    internal fun debugValueString(): String {
        return if (isImage) "(Image) ${images[0].replace(' ', '_').urlEncode()}"
        else if (isFile) "(File) ${files[0]}"
        else if (isPageReference) "(Page) ${pageReferences[0]}"
        else if (isDate) WikiAlternateDateFormatter.format(WikiDoubleBracketDateFormatter.parse(value))
        else value
    }

    private fun parsePageReferences() {
        val returnList = mutableListOf<String>()
        val matches = DoubleBracketsRegex.getAllMatchGroups(value).map { it.trim() }
        var matchedDate = false
        for (it in matches) {
            val trimmed = it.trim()
            val images = mutableListOf<Pair<String, String>>()
            val files = mutableListOf<Pair<String, String>>()
            if (matchedDate) {
                matchedDate = false
                continue
            }
            if (trimmed.isDateString()) {
                matchedDate = true
                isDate = true
                continue
            }

            val fileMatches = FileRegex.getAllMatchGroups(trimmed)
            if (fileMatches.isNotEmpty()) {
                for (file in fileMatches) {
                    if (file.endsWith(".png")) images.add(trimmed to file)
                    else files.add(trimmed to file)
                }
                this.images.addAll(images.map { it.second })
                this.files.addAll(files.map { it.second })
            }

            var other = trimmed
            (images + files).forEach { other = other.replace(it.second, "").replace("File:", "") }
            if (other.trim().isEmpty()) continue
            remaining = other
            returnList.add(other)
        }
        pageReferencesList.addAll(returnList)
    }

    companion object {
        internal val DoubleBracketsRegex = "\\[\\[(.*?)]]".toRegex()
        internal val FileRegex = "(?:\\[{2})?File:(.*?\\.(?:\\w*\\d*)*)(?:]{2})?(?:\\|.*)?".toRegex()
        private val DateStringRegex = "^\\d{1,2}\\s\\w+".toRegex()
        private fun String.isDateString(): Boolean {
            if (isEmpty()) return false
            return DateStringRegex.containsMatchIn(this)
        }
    }

}

