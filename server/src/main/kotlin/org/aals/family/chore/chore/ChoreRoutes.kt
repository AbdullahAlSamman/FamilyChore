package org.aals.family.chore.chore

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.aals.family.chore.core.data.remote.dto.ChoreDto
import org.aals.family.chore.core.data.remote.dto.ChoresResponse
import org.aals.family.chore.core.data.remote.dto.CreateChoreRequest
import org.aals.family.chore.core.data.remote.dto.ErrorResponse
import org.aals.family.chore.core.data.remote.dto.UpdateChoreStatusRequest
import org.aals.family.chore.core.data.remote.dto.ValidationErrorDto
import org.aals.family.chore.core.domain.model.Chore
import org.aals.family.chore.core.domain.model.ChoreStatus
import org.aals.family.chore.core.domain.validation.ChoreValidator
import org.aals.family.chore.domain.repository.ChoreRepository
import java.util.UUID

fun Route.choreRoutes(choreRepository: ChoreRepository) {
    route("/family/{familyId}/chores") {
        get {
            val familyId = call.parameters["familyId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val userId = call.request.queryParameters["userId"]
            
            val chores = if (userId != null) {
                choreRepository.getChoresByUser(userId)
            } else {
                choreRepository.getChoresByFamily(familyId)
            }
            
            call.respond(ChoresResponse(chores.map { it.toDto() }))
        }
        
        post {
            val request = call.receive<CreateChoreRequest>()
            
            val nameError = ChoreValidator.validateName(request.name)
            val pointsError = ChoreValidator.validatePoints(request.points)
            val assigneeError = ChoreValidator.validateAssignee(request.assignedTo)
            
            if (nameError != null || pointsError != null || assigneeError != null) {
                val validationErrors = mutableListOf<ValidationErrorDto>()
                nameError?.let { validationErrors.add(ValidationErrorDto("name", it.name, "Invalid name")) }
                pointsError?.let { validationErrors.add(ValidationErrorDto("points", it.name, "Invalid points")) }
                assigneeError?.let { validationErrors.add(ValidationErrorDto("assignedTo", it.name, "Invalid assignee")) }
                
                return@post call.respond(
                    HttpStatusCode.BadRequest, 
                    ErrorResponse("Validation failed", validationErrors = validationErrors)
                )
            }

            val now = System.currentTimeMillis()
            val chore = Chore(
                id = UUID.randomUUID().toString(),
                familyId = request.familyId,
                name = request.name,
                description = request.description,
                points = request.points,
                status = ChoreStatus.PENDING,
                assignedTo = request.assignedTo,
                createdBy = request.createdBy,
                createdAt = now,
                updatedAt = now
            )
            val createdChore = choreRepository.createChore(chore)
            call.respond(HttpStatusCode.Created, createdChore.toDto())
        }

        patch("/{choreId}/status") {
            val familyId = call.parameters["familyId"] ?: return@patch call.respond(HttpStatusCode.BadRequest)
            val choreId = call.parameters["choreId"] ?: return@patch call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<UpdateChoreStatusRequest>()
            
            val success = choreRepository.updateChoreStatus(
                choreId = choreId,
                familyId = familyId,
                newStatus = request.newStatus,
                adminId = request.adminId
            )
            
            if (success) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

fun Chore.toDto(): ChoreDto = ChoreDto(
    id = id,
    familyId = familyId,
    name = name,
    description = description,
    points = points,
    status = status,
    assignedTo = assignedTo,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt
)
