package org.tribot.wikiscraper

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.tribot.wikiscraper.query.WikiQuery
import org.tribot.wikiscraper.utility.*
import org.tribot.wikiscraper.utility.GSON
import org.tribot.wikiscraper.utility.getString
import org.tribot.wikiscraper.utility.propertyMap
import java.net.CookieManager
import java.net.Proxy
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


/* Written by IvanEOD 12/9/2022, at 8:19 AM */
class OsrsWiki private constructor() {

    internal val configuration = Configuration()
    internal lateinit var client: Client
        private set
    internal lateinit var namespaceManager: Namespace.Manager
        private set

    internal fun newQuery(vararg templates: WikiQuery.Template): WikiQuery = WikiQuery(this, *templates)
    
    private fun refreshNamespaceManager() {
        namespaceManager = Namespace.Manager(newQuery(WikiQuery.Namespaces).next()!!
            .input.getAsJsonObject("query"))
    }

    fun basicGet(action: String, vararg parameters: String): Response? {
        val properties = propertyMap(*parameters)
        properties["action"] = action
        properties["format"] = "json"
        return try {
            client.basicGet(properties)
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
    private fun postAction(action: String, form: Map<String, String>): RequestResult {
        val properties = mutableMapOf("format" to "json")
        properties.putAll(form)
        return try {
            val response = client.basicPost(mutableMapOf("action" to action), properties).body?.string() ?: ""
            if (response.isEmpty()) RequestResult.None
            else {
                val result = JsonParser.parseString(response).asJsonObject
                RequestResult.wrap(result, action)
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            RequestResult.None
        }
    }

    internal fun ItemBuyLimits.Companion.fetch(): ItemBuyLimits {
        val response = client.newCall(BuyLimitsUrl).body?.string() ?: ""
        if (response.isEmpty()) throw Exception("Failed to fetch buy limits")
        val result = mutableMapOf<String, Int>()
        val lines = response.split("\n")
        var lastUpdateLong: Long = 0L
        var lastUpdateDateString: String = ""
        for (line in lines) {
            val matches = BuyLimitsRegex.getAllMatchGroups(line)
            println("Matches Size: ${matches.size}")
            if (matches.size >= 2) {
                val key = matches[1]
                val value = matches[2]
                if (key == "%LAST_UPDATE%") lastUpdateLong = value.toLong()
                if (key == "%LAST_UPDATE_F%") lastUpdateDateString = value
                else result[key] = value.toIntOrNull() ?: -1
            }
        }
        return ItemBuyLimits(lastUpdateLong, lastUpdateDateString, result)
    }
    internal fun ItemBuyLimits.Companion.fetchLastUpdate(): Long {
        val response = client.newCall(BuyLimitsUrl).body?.string() ?: ""
        if (response.isEmpty()) throw Exception("Failed to fetch buy limits")
        val lines = response.split("\n")
        for (line in lines) {
            val matches = BuyLimitsRegex.getAllMatchGroups(line)
            if (matches.isNotEmpty()) {
                val key = matches[0]
                val value = matches[1]
                if (key == "%LAST_UPDATE%") return value.toLong()
            }
        }
        return 0
    }

    inner class Builder internal constructor() {

        fun withCookieManager(cookieManager: CookieManager): Builder {
            configuration.cookieManager = cookieManager
            return this
        }

        fun withProxy(proxy: Proxy): Builder {
            configuration.proxy = proxy
            return this
        }

        fun withUserAgent(userAgent: String): Builder {
            configuration.userAgent = userAgent
            return this
        }

        fun build(): OsrsWiki {
            client = Client(configuration)
            refreshNamespaceManager()
            return this@OsrsWiki
        }

    }
    inner class Client internal constructor(configuration: Configuration) {
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
            val Main: Namespace = Namespace(0)
            val Talk: Namespace = Namespace(1)
            val User: Namespace = Namespace(2)
            val UserTalk: Namespace = Namespace(3)
            val Project: Namespace = Namespace(4)
            val ProjectTalk: Namespace = Namespace(5)
            val File: Namespace = Namespace(6)
            val FileTalk: Namespace = Namespace(7)
            val Mediawiki: Namespace = Namespace(8)
            val MediawikiTalk: Namespace = Namespace(9)
            val Template: Namespace = Namespace(10)
            val TemplateTalk: Namespace = Namespace(11)
            val Help: Namespace = Namespace(12)
            val HelpTalk: Namespace = Namespace(13)
            val Category: Namespace = Namespace(14)
            val CategoryTalk: Namespace = Namespace(15)
        }

        override fun hashCode(): Int = value
        override fun equals(other: Any?): Boolean = other is Namespace && other.value == value

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

            fun createFilter(vararg namespaces: Namespace): String = namespaces.map { it.value.toString() }.pipeFence()

            fun convertIfNotInNamespace(title: String, namespace: Namespace): String =
                if (whichNamespace(title) == namespace) title
                else String.format("%s:%s", namespacesMap[namespace.value], strip(title), title)
            fun getNamespace(prefix: String): Namespace =
                if (prefix.isEmpty() || prefix.equals("main", true)) Main
                else if (prefix in namespacesMap) Namespace(namespacesMap[prefix] as Int) else Main
            fun whichNamespace(title: String): Namespace {
                val matcher = pattern.matcher(title)
                return if (matcher.find())
                    Namespace(namespacesMap[title.substring(matcher.start(), matcher.end() - 1).toIntOrNull() ?: 0] as Int)
                else Main
            }
            fun strip(title: String) = title.replace(namespaceRegex, "")
            fun strip(titles: Collection<String>) = titles.map { strip(it) }
            fun filterByNamespace(titles: Collection<String>, namespace: Namespace) =
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
            fun wrap(jsonObject: JsonObject, action: String): RequestResult {
                try {
                    if (jsonObject.has(action)) {
                        when (jsonObject.getAsJsonObject(action).getString("result")) {
                            "Success" -> return Success
                            else -> println("Something isn't right. Got ${GSON.toJson(jsonObject)}")
                        }
                    } else if (jsonObject.has("error")) {
                        return when (jsonObject.getAsJsonObject("error").getString("code")) {
                            "badtoken" -> BadToken
                            "notoken" -> NoToken
                            "NeedToken" -> NeedToken
                            "cascadeprotected",
                            "protectedpage" -> Protected
                            "ratelimited" -> RateLimited
                            else -> {
                                println("Something isn't right. Got ${GSON.toJson(jsonObject)}")
                                Error
                            }
                        }
                    } else println("Something isn't right. Got ${GSON.toJson(jsonObject)}")
                } catch (error: Throwable) {
                    println("Throwable: ${error.message}")
                    error.printStackTrace()
                }
                return None
            }
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = OsrsWiki().Builder()

    }

}