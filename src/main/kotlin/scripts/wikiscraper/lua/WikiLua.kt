package scripts.wikiscraper.lua

/* Written by IvanEOD 12/10/2022, at 4:13 PM */
sealed class ScribuntoError(message: String): Throwable(message) {
    override fun toString(): String = message ?: "Unknown Scribunto Error"
}
class ScribuntoRequestError(sessionId: Int, message: String): ScribuntoError("Scribunto request error: (Session: $sessionId): $message")
class ScribuntoLuaError(sessionId: Int, message: String): ScribuntoError("Scribunto Lua error (Session: $sessionId): $message")
class ScribuntoGeneralError(sessionId: Int, message: String): ScribuntoError("Scribunto error (Session: $sessionId): $message")
class ScribuntoSessionError(sessionId: Int, message: String): ScribuntoError("Scribunto session error (Session: $sessionId): $message")
class ScribuntoSessionSizeTooLarge(sessionId: Int): ScribuntoError("Scribunto Error (Session: $sessionId): Session size is too large")
class ScribuntoUnknownError(sessionId: Int): ScribuntoError("Scribunto Error (Session: $sessionId): Unknown cause")
