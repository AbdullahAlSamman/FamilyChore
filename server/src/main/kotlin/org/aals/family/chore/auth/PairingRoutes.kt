package org.aals.family.chore.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.aals.family.chore.core.data.remote.dto.*
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.domain.repository.FamilyRepository

fun Route.pairingRoutes(
    familyRepository: FamilyRepository,
    pairingManager: PairingManager
) {
    route("/auth") {
        post("/family/create") {
            val request = call.receive<CreateFamilyRequest>()
            val family = familyRepository.createFamily(request.familyName)
            val parent = familyRepository.addUserToFamily(family.id, request.parentNickname, UserRole.PARENT)
            
            // For now, "token" is just a dummy JWT
            call.respond(
                CreateFamilyResponse(
                    familyId = family.id,
                    parentUser = parent,
                    token = "dummy-jwt-${parent.id}"
                )
            )
        }

        post("/pair/generate") {
            val request = call.receive<GeneratePairingTokenRequest>()
            // In a real app, we'd verify the requester is a parent in that family
            val token = pairingManager.generateToken(request.familyId)
            call.respond(GeneratePairingTokenResponse(token))
        }

        get("/pair/users") {
            val token = call.request.queryParameters["token"]
            if (token == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing token")
                return@get
            }
            val familyId = pairingManager.validateToken(token)
            if (familyId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid or expired token")
                return@get
            }

            val family = familyRepository.getFamily(familyId)
            val users = familyRepository.getUsersInFamily(familyId)
            
            call.respond(
                mapOf(
                    "familyName" to family?.name,
                    "users" to users
                )
            )
        }

        post("/pair/confirm") {
            val request = call.receive<ConfirmPairingRequest>()
            val familyId = pairingManager.validateToken(request.pairingToken)
            
            if (familyId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid or expired pairing token")
                return@post
            }

            val user = familyRepository.getUser(request.userId)
            if (user == null || user.familyId != familyId) {
                call.respond(HttpStatusCode.BadRequest, "User not found in this family")
                return@post
            }

            pairingManager.consumeToken(request.pairingToken)

            call.respond(
                ConfirmPairingResponse(
                    familyId = familyId,
                    user = user,
                    token = "dummy-jwt-${user.id}"
                )
            )
        }
    }
}
