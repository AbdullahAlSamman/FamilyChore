package org.aals.family.chore

import co.touchlab.kermit.Logger
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.aals.family.chore.auth.PairingManager
import org.aals.family.chore.auth.pairingRoutes
import org.aals.family.chore.chore.choreRoutes
import org.aals.family.chore.data.local.DatabaseFactory
import org.aals.family.chore.data.repository.SqlChoreRepository
import org.aals.family.chore.data.repository.SqlFamilyRepository
import org.aals.family.chore.data.repository.SqlTransactionRepository
import org.aals.family.chore.transaction.transactionRoutes

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
    val transactionRepository = SqlTransactionRepository()
    val choreRepository = SqlChoreRepository()
    val pairingManager = PairingManager()

    routing {
        get("/") {
            call.respondText("FamilyChore Server Running")
        }

        pairingRoutes(familyRepository, pairingManager)
        transactionRoutes(transactionRepository)
        choreRoutes(choreRepository)
    }
}
