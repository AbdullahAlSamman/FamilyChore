package org.aals.family.chore

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.aals.family.chore.auth.PairingManager
import org.aals.family.chore.auth.pairingRoutes
import org.aals.family.chore.core.data.local.entity.ChoreEntity
import org.aals.family.chore.data.local.DatabaseFactory
import org.aals.family.chore.data.repository.SqlFamilyRepository
import co.touchlab.kermit.Logger

fun main() {
    val port = 8080
    
    DatabaseFactory.init()
    
    Logger.i { "Starting FamilyChore Server on port $port" }
    
    val broadcaster = DiscoveryBroadcaster()
    broadcaster.start(port)

    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    val familyRepository = SqlFamilyRepository()
    val pairingManager = PairingManager()

    routing {
        get("/") {
            call.respondText("FamilyChore Server Running")
        }

        pairingRoutes(familyRepository, pairingManager)

        route("/{familyId}") {
            get("/chores") {
                val familyId = call.parameters["familyId"]
                // In a real implementation, we would fetch from a database scoped by familyId
                // For now, we return an empty list scoped by familyId
                call.respond(emptyList<ChoreEntity>())
            }
        }
    }
}
