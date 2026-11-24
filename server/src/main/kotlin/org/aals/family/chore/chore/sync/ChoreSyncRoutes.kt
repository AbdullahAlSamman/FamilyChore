package org.aals.family.chore.chore.sync

import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.close
import io.ktor.websocket.CloseReason
import kotlinx.coroutines.channels.consumeEach
import co.touchlab.kermit.Logger

fun Route.choreSyncRoutes(sessionManager: ChoreSessionManager) {
    route("/family/{familyId}/chores/ws") {
        webSocket {
            val familyId = call.parameters["familyId"]
            if (familyId == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing familyId"))
                return@webSocket
            }

            sessionManager.addSession(familyId, this)
            try {
                // Keep the connection open and consume incoming messages (if any, like keep-alives)
                incoming.consumeEach { frame ->
                    // Just log or handle pings if needed
                    Logger.d { "Received frame from client for family $familyId" }
                }
            } catch (e: Exception) {
                Logger.e(e) { "WebSocket connection closed with error for family $familyId" }
            } finally {
                sessionManager.removeSession(familyId, this)
            }
        }
    }
}
