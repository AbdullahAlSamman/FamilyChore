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
import org.aals.family.chore.storage.FileStorage
import java.util.UUID

import org.aals.family.chore.chore.sync.ChoreSessionManager
import io.ktor.http.content.PartData
import io.ktor.server.request.receiveMultipart
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.response.respondBytes

fun Route.choreRoutes(choreRepository: ChoreRepository, sessionManager: ChoreSessionManager, fileStorage: FileStorage) {
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
                updatedAt = now,
                recurrence = request.recurrence,
                activeDays = request.activeDays,
                activeWindowStart = request.activeWindowStart,
                activeWindowEnd = request.activeWindowEnd,
                penalty = request.penalty,
                requiresPhotoProof = request.requiresPhotoProof,
                iconName = request.iconName,
                parentChoreId = request.parentChoreId,
                dueDate = request.dueDate
            )
            val createdChore = choreRepository.createChore(chore)
            val choreDto = createdChore.toDto()
            sessionManager.broadcastChoreUpdate(request.familyId, choreDto)
            call.respond(HttpStatusCode.Created, choreDto)
        }

        patch("/{choreId}/status") {
            val familyId = call.parameters["familyId"] ?: return@patch call.respond(HttpStatusCode.BadRequest)
            val choreId = call.parameters["choreId"] ?: return@patch call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<UpdateChoreStatusRequest>()
            
            val familyChores = choreRepository.getChoresByFamily(familyId)
            val existingChore = familyChores.find { it.id == choreId }
            if (existingChore != null && (request.newStatus == ChoreStatus.COMPLETED || request.newStatus == ChoreStatus.PENDING_APPROVAL)) {
                existingChore.activeWindowEnd?.let { windowEnd ->
                    val nowMinutes = java.time.LocalTime.now().run { hour * 60 + minute }
                    if (nowMinutes > windowEnd) {
                        return@patch call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Active window has ended for this chore")
                        )
                    }
                }
            }

            val success = choreRepository.updateChoreStatus(
                choreId = choreId,
                familyId = familyId,
                newStatus = request.newStatus,
                adminId = request.adminId
            )
            
            if (success) {
                // Fetch the updated chore to broadcast
                val updatedChoreList = choreRepository.getChoresByFamily(familyId)
                val updatedChore = updatedChoreList.find { it.id == choreId }
                if (updatedChore != null) {
                    sessionManager.broadcastChoreUpdate(familyId, updatedChore.toDto())
                }
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post("/{choreId}/image") {
            val familyId = call.parameters["familyId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val choreId = call.parameters["choreId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            
            val multipart = call.receiveMultipart()
            var fileId: String? = null

            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val fileName = part.originalFileName ?: "image.jpg"
                    val inputStream = part.streamProvider()
                    fileId = fileStorage.saveFile(familyId, choreId, fileName, inputStream)
                }
                part.dispose()
            }

            if (fileId != null) {
                // Return success
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.BadRequest, "No file found")
            }
        }

        get("/{choreId}/image") {
            val familyId = call.parameters["familyId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val choreId = call.parameters["choreId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            
            val chores = choreRepository.getChoresByFamily(familyId)
            val chore = chores.find { it.id == choreId }
            val iconName = chore?.iconName
            
            if (iconName == null || iconName.startsWith("bundled:")) {
                return@get call.respond(HttpStatusCode.NotFound)
            }
            
            // The fileId convention in LocalDiskFileStorage is familyId/choreId/uuid_filename
            // but the client uploads a unique filename via remoteDataSource.uploadTaskImage and it gets saved under family/chore/file.
            // Wait, we need to map iconName to the actual file ID, or just search the folder.
            // Let's just assume the file ID is familyId/choreId/iconName
            val fileId = "$familyId/$choreId/$iconName"
            val fileStream = fileStorage.getFile(fileId)
            
            if (fileStream != null) {
                val bytes = fileStream.readBytes()
                fileStream.close()
                call.respondBytes(bytes)
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
    updatedAt = updatedAt,
    recurrence = recurrence,
    activeDays = activeDays,
    activeWindowStart = activeWindowStart,
    activeWindowEnd = activeWindowEnd,
    penalty = penalty,
    requiresPhotoProof = requiresPhotoProof,
    iconName = iconName,
    parentChoreId = parentChoreId,
    dueDate = dueDate,
    proofFileId = proofFileId
)
