package scripts.wikiscraper.lua


import scripts.wikiscraper.OsrsWiki
import java.io.File
import java.util.concurrent.ConcurrentLinkedDeque


/* Written by IvanEOD 12/18/2022, at 12:04 PM */
internal class SessionManager internal constructor(private val wiki: OsrsWiki) {
    private val sessions = ConcurrentLinkedDeque<ScribuntoSession>()

    val session: ScribuntoSession get() = sessions.maxByOrNull { it.successfulRequests } ?: createSession()
    fun refreshSession(id: Int) = sessions.find { it.id == id }?.refresh()

    fun freshSession(sessionModule: String? = null, code: String? = null): ScribuntoSession = this[-1, sessionModule, code]!!

    fun createSession(block: ScribuntoSession.Builder.() -> Unit): ScribuntoSession {
        val builder = ScribuntoSession.Builder()
        builder.block()
        return builder.build()
    }

    private fun createSession(sessionModule: String? = null, code: String? = null): ScribuntoSession {
        val session = ScribuntoSession(wiki, sessionModule, code)
        this += session
        return session
    }

    operator fun get(id: Int?, module: String? = null, code: String? = null): ScribuntoSession? = if (id == null || id == -1) createSession(module, code)
        else sessions.firstOrNull { it.isSession(id) }

    operator fun minusAssign(id: Int) {
        this[id]?.let { sessions.remove(it) }
    }
    operator fun plusAssign(session: ScribuntoSession) {
        if (sessions.any { it.id == session.id }) return
        sessions.add(session)
    }



    fun ScribuntoSession.Builder.build(): ScribuntoSession {
        val sessionCode = "${if (includeDefaultLua) loadMainLua() else DefaultLuaMinimum}\n\n$code"
        return freshSession(module, sessionCode)
    }

    companion object {
        private val DefaultLuaMinimum = """
            function printReturn(value, error, message)
                local response = {}
                response['success'] = error == nil or not error
                if (error == true and message ~= nil) then
                    response['message'] = message
                end
                response['printReturn'] = value
                print(toJson(response))
            end
            
            function isSessionLoaded()
                printReturn(true)
            end
            """.trimIndent()

        internal fun loadMainLua(): String {
            return File("src/main/kotlin/org/tribot/wikiscraper/lua/Scribunto.lua").readText()
        }
    }

}


