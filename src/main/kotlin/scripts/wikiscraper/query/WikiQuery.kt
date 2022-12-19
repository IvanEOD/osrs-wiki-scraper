package scripts.wikiscraper.query

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import scripts.wikiscraper.OsrsWiki
import scripts.wikiscraper.utility.*
import scripts.wikiscraper.utility.GSON
import scripts.wikiscraper.utility.hasNested
import scripts.wikiscraper.utility.pipeFence
import scripts.wikiscraper.utility.propertyMap
import java.lang.reflect.Type


/* Written by IvanEOD 12/9/2022, at 8:34 AM */
class WikiQuery(
    private val wiki: OsrsWiki,
    private val totalLimit: Int = 500,
    private vararg val templates: Template
) {
    constructor(wiki: OsrsWiki, vararg templates: Template) : this(wiki, 500, *templates)

    private val properties = propertyMap("action", "query", "format", "json")
    private val limitStringList = mutableListOf<String>()
    private var canContinue = true
    private var queryLimit = 500
    private var currentCount = 0

    init {
        for (template in templates) {
            properties.putAll(template.fields)
            if (template.limitString != null) limitStringList.add(template.limitString)
        }
    }

    fun isNotEmpty(): Boolean = canContinue
    fun isEmpty() = !canContinue

    fun next(): Reply? = if (isEmpty()) null
    else try {
        if (totalLimit > 0 && (currentCount + queryLimit) > totalLimit) {
            currentCount += queryLimit
            adjustLimit(queryLimit - (currentCount - totalLimit))
            canContinue = false
        }
        val response = wiki.client.basicGet(properties).body?.string() ?: ""
        val result = JsonParser.parseString(response).asJsonObject
        if (result.has("continue")) properties.putAll(GSON.fromJson(result.getAsJsonObject("continue"), StringMapType))
        else canContinue = false
        Reply(result)
    } catch (throwable: Throwable) {
        throwable.printStackTrace()
        null
    }

    operator fun set(key: String, value: String): WikiQuery {
        properties[key] = value
        return this
    }

    operator fun set(key: String, values: List<String>): WikiQuery {
        properties[key] = values.pipeFence()
        return this
    }

    private fun adjustLimit(limit: Int) {
        val limitString: String
        if (limit <= 0 || limit > wiki.configuration.maxResultLimit) {
            limitString = "max"
            queryLimit = wiki.configuration.maxResultLimit
        } else {
            limitString = limit.toString()
            queryLimit = limit
        }
        for (string in limitStringList) properties[string] = limitString
    }

    data class Template(
        private var defaultFields: Map<String, String>,
        internal val limitString: String?,
        internal val id: String?
    ) {
        val fields: MutableMap<String, String> = defaultFields.toMutableMap()
        constructor(defaultFields: Map<String, String>, id: String?) : this(defaultFields, null, id)
        init {
            if (limitString != null) fields[limitString] = "max"
        }
    }
    class Reply internal constructor(internal val input: JsonObject) {
        private var normalized: MutableMap<String, String> = mutableMapOf()

        init {
            if (input.hasNested("query", "normalized"))
                normalized = input.getNestedJsonArray("query", "normalized").toJsonObjectsList().mappedBy("from", "to")
        }

        fun listComprehension(key: String): MutableList<JsonObject> =
            if (input.has("query")) input.getAsJsonObject("query").toJsonObjectsList(key)
            else mutableListOf()
        fun propertyComprehension(key: String, value: String): MutableMap<String, JsonElement?> {
            val map = mutableMapOf<String, JsonElement?>()
            val jsonObject = input.getNestedJsonObject(*defaultPropPTJ.toTypedArray()) ?: return map
            for (element in jsonObject.toJsonObjectsList()) map[element.getString(key)] = element.get(value)
            return normalize(map)
        }
        fun metaComprehension(key: String): JsonElement = if (input.has("query")) input.getAsJsonObject("query").get(key) else JsonObject()

        private fun <V> normalize(map: MutableMap<String, V?>): MutableMap<String, V?> {
            if (normalized.isNotEmpty()) normalized.forEach { (f, t) -> if (map.containsKey(t)) map[f] = map[t]!! }
            return map
        }
        companion object {
            internal val defaultPropPTJ = mutableListOf("query", "pages")
        }

    }
    companion object {
        val AllowedFileExtensions = Template(propertyMap("meta", "siteinfo", "siprop", "fileextensions"), "fileextensions")
        val AllPages = Template(propertyMap("list", "allpages"), "aplimit", "allpages")
        val CategoryInfo = Template(propertyMap("prop", "categoryinfo", "titles", ""), "categoryinfo")
        val CategoryMembers = Template(propertyMap("list", "categorymembers", "cmtitle", ""), "cmlimit", "categorymembers")
        val Namespaces = Template(propertyMap("meta", "siteinfo", "siprop", "namespaces|namespacealiases"), "")
        val DuplicateFiles = Template(propertyMap("prop", "duplicatefiles", "titles", ""), "dflimit", "duplicatefiles")
        val Exists = Template(propertyMap("prop", "pageprops", "ppprop", "missing", "titles", ""), "")
        val ExternalLinks = Template(propertyMap("prop", "extlinks", "elexpandurl", "1", "titles", ""), "ellimit", "extlinks")
        val FileUsage = Template(propertyMap("prop", "fileusage", "titles", ""), "fulimit", "fileusage")
        val GlobalUsage = Template(propertyMap("prop", "globalusage", "titles", ""), "gulimit", "globalusage")
        val Images = Template(propertyMap("prop", "images", "titles", ""), "imlimit", "images")
        val ImageInfo = Template(propertyMap("prop", "imageinfo", "iiprop", "canonicaltitle|url|size|sha1|mime|user|timestamp|comment", "titles", ""), "iilimit", "imageinfo")
        val LastRevisionTimestamp = Template(propertyMap("prop", "revisions", "rvprop", "timestamp", "titles", ""), "")
        val LinksHere = Template(propertyMap("prop", "linkshere", "lhprop", "title", "lhshow", "", "titles", ""), "lhlimit", "linkshere")
        val LinksOnPage = Template(propertyMap("prop", "links", "titles", ""), "pllimit", "links")
        val LogEvents = Template(propertyMap("list", "logevents"), "lelimit", "logevents")
        val PageCategories = Template(propertyMap("prop", "categories", "titles", ""), "cllimit", "categories")
        val PageText = Template(propertyMap("prop", "revisions", "rvprop", "content", "titles", ""), "")
        val ProtectedTitles = Template(propertyMap("list", "protectedtitles", "ptprop", "timestamp|level|user|comment"), "ptlimit", "protectedtitles")
        val QueryPages = Template(propertyMap("list", "querypage", "qppage", ""), "qplimit", "querypage")
        val Random = Template(propertyMap("list", "random", "rnfilterredir", "nonredirects"), "rnlimit", "random")
        val RecentChanges = Template(propertyMap("list", "recentchanges", "rcprop", "title|timestamp|user|comment", "rctype", "edit|new|log"), "rclimit", "recentchanges")
        val ResolveRedirect = Template(propertyMap("redirects", "", "titles", ""), "redirects")
        val Revisions = Template(propertyMap("prop", "revisions", "rvprop", "comment|content|ids|timestamp|user", "titles", ""), "rvlimit", "revisions")
        val Search = Template(propertyMap("list", "search", "srprop", "", "srnamespace", "*", "srsearch", ""), "srlimit", "search")
        val Templates = Template(propertyMap("prop", "templates", "tiprop", "title", "titles", ""), "tllimit", "templates")
        val TextExtracts = Template(propertyMap("prop", "extracts", "exintro", "1", "explaintext", "1", "titles", ""), "exlimit", "extract")
        val TokensCsrf = Template(propertyMap("meta", "tokens", "type", "csrf"), "")
        val TokensLogin = Template(propertyMap("meta", "tokens", "type", "login"), "")
        val TranscludedIn = Template(propertyMap("prop", "transcludedin", "tiprop", "title", "titles", ""), "tilimit", "transcludedin")
        val UserContributions = Template(propertyMap("list", "usercontribs", "ucuser", ""), "uclimit", "usercontribs")
        val UserInfo = Template(propertyMap("meta", "userinfo"), "")
        val UserRights = Template(propertyMap("list", "users", "usprop", "groups", "ususers", ""), "users")
        val UserUploads = Template(propertyMap("list", "allimages", "aisort", "timestamp", "aiuser", ""), "ailimit", "allimages")
        val Ask = Template(propertyMap("action", "ask", "format", "json", "query", ""), "")
        private val StringMapType: Type = object : TypeToken<MutableMap<String, String>>() {}.type
    }

}