package org.aals.family.chore.transaction

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.domain.repository.TransactionRepository

fun Route.transactionRoutes(transactionRepository: TransactionRepository) {
    route("/{familyId}/transactions") {
        get {
            val familyId = call.parameters["familyId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val userId = call.request.queryParameters["userId"]
            
            val transactions = if (userId != null) {
                transactionRepository.getTransactionsForUser(userId)
            } else {
                transactionRepository.getTransactionsForFamily(familyId)
            }
            
            call.respond(transactions)
        }
        
        post {
            val transaction = call.receive<Transaction>()
            transactionRepository.addTransaction(transaction)
            call.respond(HttpStatusCode.Created)
        }
    }
}
