package org.tribot.wikiscraper.lua


/* Written by IvanEOD 12/10/2022, at 4:13 PM */

sealed class ScribuntoError(message: String): Throwable(message) {

    override fun toString(): String = message ?: "Unknown Scribunto Error"
}
class ScribuntoRequestError(message: String): ScribuntoError("Scribunto request error: $message")
class ScribuntoLuaError(message: String): ScribuntoError("Scribunto Lua error: $message")
class ScribuntoGeneralError(message: String): ScribuntoError("Scribunto error: $message")
object ScribuntoSessionSizeTooLarge: ScribuntoError("Scribunto Error: Session size is too large")
object ScribuntoUnknownError: ScribuntoError("Scribunto Error: Unknown cause")
