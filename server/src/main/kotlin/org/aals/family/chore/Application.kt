package org.aals.family.chore

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.aals.family.chore.core.data.local.entity.ChoreEntity

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/") {
            call.respondText("FamilyChore Server Running")
        }

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
