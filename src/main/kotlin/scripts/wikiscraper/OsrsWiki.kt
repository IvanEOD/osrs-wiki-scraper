package scripts.wikiscraper

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import scripts.wikiscraper.classes.ItemBuyLimits
import scripts.wikiscraper.lua.LuaGlobalScope
import scripts.wikiscraper.lua.ScribuntoSession
import scripts.wikiscraper.lua.SessionManager
import scripts.wikiscraper.lua.lua
import scripts.wikiscraper.query.WikiQuery
import scripts.wikiscraper.utility.*
import scripts.wikiscraper.utility.pipeFence
import scripts.wikiscraper.utility.propertyMap
import scripts.wikiscraper.utility.toJsonObjectsList
import java.net.CookieManager
import java.net.Proxy
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


/* Written by IvanEOD 12/9/2022, at 8:19 AM */
class OsrsWiki private constructor() {

    internal val configuration = Configuration()
    internal lateinit var client: scripts.wikiscraper.OsrsWiki.Client
        private set
    internal lateinit var namespaceManager: scripts.wikiscraper.OsrsWiki.Namespace.Manager
        private set

    private lateinit var sessionManager: SessionManager
    lateinit var scribuntoSession: ScribuntoSession
        private set

    private val bulkScribuntoSessions = mutableListOf<ScribuntoSession>()
    private var bulkScribuntoSessionIndex = 0
    private var defaultBulkSessionCount = 10

    internal fun newQuery(vararg templates: WikiQuery.Template): WikiQuery = WikiQuery(this, *templates)


    fun createScribuntoSession() = sessionManager.freshSession()
    fun createScribuntoSession(block: ScribuntoSession.Builder.() -> Unit) = sessionManager.createSession(block)

    fun scribunto(block: LuaGlobalScope.() -> Unit) = scribunto(false, block)
    fun scribunto(code: String) = scribunto(false, code)
    fun scribunto(refreshSession: Boolean, block: LuaGlobalScope.() -> Unit) = scribuntoSession.sendRequest(refreshSession, block)
    fun scribunto(refreshSession: Boolean, code: String) = scribuntoSession.sendRequest(refreshSession, code)

    fun bulkScribunto(sessions: Int = defaultBulkSessionCount, block: LuaGlobalScope.() -> Unit): Pair<Boolean, JsonElement> =
        bulkScribunto(sessions, lua(block))
    fun bulkScribunto(sessions: Int = defaultBulkSessionCount, code: String): Pair<Boolean, JsonElement> {
        if (bulkScribuntoSessions.size < sessions) {
            bulkScribuntoSessions.addAll((bulkScribuntoSessions.size until sessions).map { sessionManager.freshSession() })
        }
        if (bulkScribuntoSessionIndex >= bulkScribuntoSessions.size) bulkScribuntoSessionIndex = 0
        val result = bulkScribuntoSessions[bulkScribuntoSessionIndex++].sendRequest(false, code)
        bulkScribuntoSessionIndex++
        return result
    }

    
    private fun refreshNamespaceManager() {
        namespaceManager = OsrsWiki.Namespace.Manager(
            newQuery(WikiQuery.Namespaces).next()!!
                .input.getAsJsonObject("query")
        )
    }

    fun basicGet(action: String, vararg parameters: String): Response? = basicGet(action, propertyMap(*parameters))
    fun basicGet(action: String, parameters: Map<String, String>): Response? {
        val properties = parameters.toMutableMap()
        properties["action"] = action
        properties["format"] = "json"
        return try {
            client.basicGet(properties)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            null
        }
    }

    fun formGet(action: String, form: Map<String, String>): Response? {
        return try {
            client.formGet(propertyMap("action", action, "format", "json"), form)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            null
        }
    }

    fun basicPost(action: String, form: Map<String, String>): Response? {
        return try {
            client.basicPost(propertyMap("action", action, "format", "json"), form)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            null
        }
    }
    private fun postAction(action: String, form: Map<String, String>): OsrsWiki.RequestResult {
        val properties = mutableMapOf("format" to "json")
        properties.putAll(form)
        return try {
            val response = client.basicPost(mutableMapOf("action" to action), properties).body?.string() ?: ""
            if (response.isEmpty()) OsrsWiki.RequestResult.None
            else {
                val result = JsonParser.parseString(response).asJsonObject
                OsrsWiki.RequestResult.Companion.wrap(result, action)
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            OsrsWiki.RequestResult.None
        }
    }

    internal fun ItemBuyLimits.Companion.fetch(): ItemBuyLimits {
        val response = client.newCall(BuyLimitsUrl).body?.string() ?: ""
        if (response.isEmpty()) throw Exception("Failed to fetch buy limits")
        val result = mutableMapOf<String, Int>()
        val lines = response.split("\n")
        var lastUpdateLong = 0L
        var lastUpdateDateString = ""
        for (line in lines) {
            val matches = BuyLimitsRegex.getAllMatchGroups(line)
            if (matches.size >= 2) {
                val key = matches[0]
                val value = matches[1]
                if (key == "%LAST_UPDATE%") lastUpdateLong = value.toLong()
                if (key == "%LAST_UPDATE_F%") lastUpdateDateString = value
                else result[key] = value.toIntOrNull() ?: -1
            }
        }
        return ItemBuyLimits(lastUpdateLong, lastUpdateDateString, result)
    }

    inner class Builder internal constructor() {

        fun withCookieManager(cookieManager: CookieManager): OsrsWiki.Builder {
            configuration.cookieManager = cookieManager
            return this
        }

        fun withProxy(proxy: Proxy): OsrsWiki.Builder {
            configuration.proxy = proxy
            return this
        }

        fun withUserAgent(userAgent: String): OsrsWiki.Builder {
            configuration.userAgent = userAgent
            return this
        }

        fun withBulkSessionCount(count: Int): OsrsWiki.Builder {
            defaultBulkSessionCount = count
            return this
        }

        fun build(): OsrsWiki {
            client = Client(configuration)
            refreshNamespaceManager()

            sessionManager = SessionManager(this@OsrsWiki)
            scribuntoSession = createScribuntoSession()
            return this@OsrsWiki
        }

    }
    inner class Client internal constructor(configuration: OsrsWiki.Configuration) {
        private val client = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(configuration.cookieManager))
            .readTimeout(2, TimeUnit.MINUTES)
            .apply { if (configuration.proxy != null) proxy(configuration.proxy) }
            .build()

        private fun startRequest(parameters: Map<String, String>): Request.Builder {
            val builder = configuration.baseUrl.newBuilder()
            parameters.forEach(builder::addQueryParameter)
            return Request.Builder().url(builder.build()).header("User-Agent", configuration.userAgent)
        }

        internal fun newCall(url: String, parameters: Map<String, String> = emptyMap()): Response {
            val builder = url.toHttpUrl().newBuilder()
            parameters.forEach(builder::addQueryParameter)
            val request = Request.Builder().url(builder.build()).header("User-Agent", configuration.userAgent).build()
            return client.newCall(request).execute()
        }

        internal fun basicGet(parameters: Map<String, String>): Response =
            client.newCall(startRequest(parameters).get().build()).execute()

        internal fun basicPost(parameters: Map<String, String>, form: Map<String, String>): Response = FormBody.Builder()
            .apply { form.forEach(this::add) }.let {
                client.newCall(startRequest(parameters).post(it.build()).build()).execute()
            }

        internal fun formGet(parameters: Map<String, String>, form: Map<String, String>): Response = FormBody.Builder()
            .apply { form.forEach(this::add) }.let {
                client.newCall(startRequest(parameters).post(it.build()).build()).execute()
            }

    }
    inner class Configuration internal constructor() {
        val baseUrl = "https://oldschool.runescape.wiki/api.php".toHttpUrl()
        var userAgent = "jwiki on ${System.getProperty("os.name")} ${System.getProperty("os.version")} with JVM ${System.getProperty("java.version")}"
        var cookieManager = CookieManager()
        var proxy: Proxy? = null
        var maxResultLimit = 500
        var groupQueryLimit = 50
        var maxRequestJobs = 20
    }
    class Namespace(val value: Int) {
        companion object {
            val Main: OsrsWiki.Namespace =
                OsrsWiki.Namespace(0)
            val Project: OsrsWiki.Namespace =
                OsrsWiki.Namespace(4)
            val Category: OsrsWiki.Namespace =
                OsrsWiki.Namespace(14)
        }

        override fun hashCode(): Int = value
        override fun equals(other: Any?): Boolean = other is OsrsWiki.Namespace && other.value == value

        internal class Manager(jsonObject: JsonObject) {
            internal val namespacesMap = mutableMapOf<Any, Any>()
            internal val namespaceNameList = arrayListOf<String>()
            internal var namespaceRegex: String
            internal var pattern: Pattern

            init {
                for (element in jsonObject.getAsJsonObject("namespaces").toJsonObjectsList()) {
                    var name: String = element.get("*").asString
                    if (name.isEmpty()) name = "Main"
                    val id = element.get("id").asInt
                    namespacesMap[id] = name
                    namespacesMap[name] = id
                    namespaceNameList.add(name)
                }
                for (element in jsonObject.getAsJsonArray("namespacealiases").toJsonObjectsList()) {
                    val name = element.get("*").asString
                    val id = element.get("id").asInt
                    namespacesMap[name] = id
                    namespaceNameList.add(name)
                }
                namespaceRegex =
                    String.format("(?i)^(%s):", namespaceNameList.map { it.replace(" ", "(_| )") }.pipeFence())
                pattern = Pattern.compile(namespaceRegex)
            }

            fun createFilter(vararg namespaces: OsrsWiki.Namespace): String = namespaces.map { it.value.toString() }.pipeFence()

            fun convertIfNotInNamespace(title: String, namespace: OsrsWiki.Namespace): String =
                if (whichNamespace(title) == namespace) title
                else String.format("%s:%s", namespacesMap[namespace.value], strip(title), title)
            fun getNamespace(prefix: String): OsrsWiki.Namespace =
                if (prefix.isEmpty() || prefix.equals("main", true)) OsrsWiki.Namespace.Companion.Main
                else if (prefix in namespacesMap) OsrsWiki.Namespace(
                    namespacesMap[prefix] as Int
                ) else OsrsWiki.Namespace.Companion.Main
            fun whichNamespace(title: String): OsrsWiki.Namespace {
                val matcher = pattern.matcher(title)
                return if (matcher.find())
                    OsrsWiki.Namespace(
                        namespacesMap[title.substring(
                            matcher.start(),
                            matcher.end() - 1
                        ).toIntOrNull() ?: 0] as Int
                    )
                else OsrsWiki.Namespace.Companion.Main
            }
            fun strip(title: String) = title.replace(namespaceRegex, "")
            fun strip(titles: Collection<String>) = titles.map { strip(it) }
            fun filterByNamespace(titles: Collection<String>, namespace: OsrsWiki.Namespace) =
                titles.filter { whichNamespace(it) == namespace }

        }

    }
    enum class RequestResult {
        Success,
        Error,
        None,
        BadToken,
        NoToken,
        NeedToken,
        Protected,
        RateLimited,

        ;

        companion object {
            fun wrap(jsonObject: JsonObject, action: String): OsrsWiki.RequestResult {
                try {
                    if (jsonObject.has(action)) {
                        when (jsonObject.getAsJsonObject(action).getString("result")) {
                            "Success" -> return OsrsWiki.RequestResult.Success
                            else -> println("Something isn't right. Got ${GSON.toJson(jsonObject)}")
                        }
                    } else if (jsonObject.has("error")) {
                        return when (jsonObject.getAsJsonObject("error").getString("code")) {
                            "badtoken" -> OsrsWiki.RequestResult.BadToken
                            "notoken" -> OsrsWiki.RequestResult.NoToken
                            "NeedToken" -> OsrsWiki.RequestResult.NeedToken
                            "cascadeprotected",
                            "protectedpage" -> OsrsWiki.RequestResult.Protected
                            "ratelimited" -> OsrsWiki.RequestResult.RateLimited
                            else -> {
                                println("Something isn't right. Got ${GSON.toJson(jsonObject)}")
                                OsrsWiki.RequestResult.Error
                            }
                        }
                    } else println("Something isn't right. Got ${GSON.toJson(jsonObject)}")
                } catch (error: Throwable) {
                    println("Throwable: ${error.message}")
                    error.printStackTrace()
                }
                return OsrsWiki.RequestResult.None
            }
        }
    }

    companion object {
        @JvmStatic
        fun builder(): OsrsWiki.Builder = OsrsWiki()
            .Builder()

    }

}