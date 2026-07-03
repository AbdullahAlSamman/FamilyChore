package org.aals.family.chore.auth

import co.touchlab.kermit.Logger
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.aals.family.chore.core.data.remote.dto.ConfirmPairingRequest
import org.aals.family.chore.core.data.remote.dto.ConfirmPairingResponse
import org.aals.family.chore.core.data.remote.dto.CreateFamilyRequest
import org.aals.family.chore.core.data.remote.dto.CreateFamilyResponse
import org.aals.family.chore.core.data.remote.dto.GeneratePairingTokenRequest
import org.aals.family.chore.core.data.remote.dto.GeneratePairingTokenResponse
import org.aals.family.chore.core.data.remote.dto.PairingUsersResponse
import org.aals.family.chore.core.data.remote.dto.SetupPinRequest
import org.aals.family.chore.core.data.remote.dto.VerifyPinRequest
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.domain.repository.FamilyRepository

fun Route.pairingRoutes(
    familyRepository: FamilyRepository,
    pairingManager: PairingManager
) {
    route("/auth") {
        post("/family/create") {
            val request = call.receive<CreateFamilyRequest>()
            Logger.d { "API: Creating family ${request.familyName}" }
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

        get("/family/{familyId}/users") {
            val familyId = call.parameters["familyId"]
            if (familyId == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing familyId")
                return@get
            }
            val family = familyRepository.getFamily(familyId)
            val users = familyRepository.getUsersInFamily(familyId)
            
            call.respond(
                PairingUsersResponse(
                    familyName = family?.name ?: "Unknown Family",
                    users = users
                )
            )
        }

        post("/pair/generate") {
            val request = call.receive<GeneratePairingTokenRequest>()
            Logger.d { "API: Generating pairing token for ${request.familyId}" }
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
            Logger.d { "API: Confirming pairing for user ${request.userId}" }
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

        post("/pin/setup") {
            val request = call.receive<SetupPinRequest>()
            Logger.d { "API: Setting up PIN for ${request.userId}" }
            familyRepository.setPin(request.userId, request.pin)
            call.respond(HttpStatusCode.OK)
        }

        post("/pin/verify") {
            val request = call.receive<VerifyPinRequest>()
            Logger.d { "API: Verifying PIN for ${request.userId}" }
            val isValid = familyRepository.verifyPin(request.userId, request.pin)
            if (isValid) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid PIN")
            }
        }

        get("/user/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val user = familyRepository.getUser(userId)
            if (user != null) {
                call.respond(user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
