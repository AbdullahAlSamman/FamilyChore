package org.aals.family.chore.chore.sync

import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.send
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.aals.family.chore.core.data.remote.dto.ChoreDto
import co.touchlab.kermit.Logger

class ChoreSessionManager {
    // familyId -> Set of connections
    private val sessions = mutableMapOf<String, MutableSet<DefaultWebSocketSession>>()
    private val mutex = Mutex()

    suspend fun addSession(familyId: String, session: DefaultWebSocketSession) {
        mutex.withLock {
            sessions.getOrPut(familyId) { mutableSetOf() }.add(session)
        }
        Logger.i { "Added WebSocket session for family $familyId" }
    }

    suspend fun removeSession(familyId: String, session: DefaultWebSocketSession) {
        mutex.withLock {
            val familySessions = sessions[familyId]
            if (familySessions != null) {
                familySessions.remove(session)
                if (familySessions.isEmpty()) {
                    sessions.remove(familyId)
                }
            }
        }
        Logger.i { "Removed WebSocket session for family $familyId" }
    }

    suspend fun broadcastChoreUpdate(familyId: String, chore: ChoreDto) {
        val message = Json.encodeToString(chore)
        val familySessions = mutex.withLock { sessions[familyId]?.toList() ?: emptyList() }
        Logger.i { "Broadcasting chore update to ${familySessions.size} sessions for family $familyId" }
        familySessions.forEach { session ->
            try {
                session.send(Frame.Text(message))
            } catch (e: Exception) {
                Logger.e(e) { "Error sending message to session" }
            }
        }
    }
}
