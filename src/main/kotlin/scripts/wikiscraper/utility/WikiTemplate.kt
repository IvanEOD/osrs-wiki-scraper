package scripts.wikiscraper.utility

sealed class WikiTemplate private constructor(
    val title: String,
    template: TemplateParsed
) : TemplateDelegate(), TemplateParsed by template {
    constructor(title: String, versionedMap: VersionedMap) : this(title, TemplateParsed.of(versionedMap))
    constructor(title: String, map: Map<String, String>) : this(title, TemplateParsed.of(map))

    abstract val keys: Set<String>
    abstract val isVersioned: Boolean
    fun debug() {
        debug("")
    }
    abstract fun debug(prefix: String)

    inline operator fun <reified T: Any> get(key: String): T? = parseOrNull(getStringValue(key))
    inline operator fun <reified T: Any> get(key: String, default: T): T = parse(getStringValue(key), default)

    companion object {
        fun from(title: String, versionedMap: VersionedMap): WikiTemplate =
            if (versionedMap.versions == 1) GenericTemplate(title, versionedMap.combined)
            else VersionedTemplate(title, versionedMap)
        fun from(title: String, map: Map<String, String>): WikiTemplate = fromMap(title, map)

        fun fromVersionedMap(title: String, versionedMap: VersionedMap): VersionedTemplate = VersionedTemplate(title, versionedMap)
        fun fromMap(title: String, map: Map<String, String>): GenericTemplate = GenericTemplate(title, map)
    }

}

open class VersionedTemplate internal constructor(title: String, private val versionedMap: VersionedMap): WikiTemplate(title, versionedMap) {
    val versions: Int get() = versionedMap.versions
    override val keys: Set<String> get() = versionedMap.keys
    fun getVersion(version: Int): WikiTemplate = fromMap(title, versionedMap.getVersion(version))
    fun getVersions(): List<WikiTemplate> = (1..versions).map { getVersion(it) }
    override val isVersioned: Boolean get() = true

    override fun debug(prefix: String) {
        println("${prefix}Template: $title")
        if (versions > 1) println("${prefix}Versions: $versions")
        versionedMap.debug(prefix)
    }

    fun getStringValue(key: String, version: Int): String = versionedMap[key, version]

    inline operator fun <reified T: Any> get(key: String, version: Int): T? = parseOrNull(getStringValue(key, version))
    inline operator fun <reified T: Any> get(key: String, version: Int, default: T): T = parse(getStringValue(key, version), default)
}

open class GenericTemplate internal constructor(title: String, private val map: Map<String, String>): WikiTemplate(title, map) {
    constructor(title: String, versionedMap: VersionedMap, version: Int) : this(title, versionedMap.getVersion(version))
    override val isVersioned: Boolean = false
    override val keys: Set<String> get() = map.keys

    override fun debug(prefix: String) {
        println("${prefix}Template: $title")
        map.forEach { println("$prefix    ${it.key} = ${it.value}") }
    }
}

sealed interface TemplateParsed {
    fun getStringValue(key: String): String

    private class WikiTemplateImpl(
        private val source: (String) -> String
    ): TemplateDelegate(), TemplateParsed {
        override fun getStringValue(key: String): String = source(key)
    }

    companion object {
        internal fun of(map: VersionedMap): TemplateParsed = WikiTemplateImpl { map[it] }
        internal fun of(map: Map<String, String>): TemplateParsed = WikiTemplateImpl { map[it] ?: "" }
    }
}