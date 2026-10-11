package org.aals.family.chore.chore

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondOutputStream
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.aals.family.chore.storage.FileStorage
import java.io.File
import co.touchlab.kermit.Logger

fun Route.photoProofRoutes(fileStorage: FileStorage) {
    route("/family/{familyId}/chores/{choreId}/proof") {

        post {
            val familyId = call.parameters["familyId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val choreId = call.parameters["choreId"] ?: return@post call.respond(HttpStatusCode.BadRequest)

            var fileId: String? = null

            val multipartData = call.receiveMultipart()
            multipartData.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val fileName = part.originalFileName ?: "proof.jpg"
                    fileId = fileStorage.saveFile(
                        familyId = familyId,
                        choreId = choreId,
                        fileName = fileName,
                        inputStream = part.streamProvider()
                    )
                }
                part.dispose()
            }

            if (fileId != null) {
                // Here we could update the chore to include the proofUrl/fileId
                call.respond(HttpStatusCode.Created, mapOf("fileId" to fileId))
            } else {
                call.respond(HttpStatusCode.BadRequest, "No file uploaded")
            }
        }

        get("/{fileId...}") {
            val fileIdParams = call.parameters.getAll("fileId")
            if (fileIdParams.isNullOrEmpty()) {
                return@get call.respond(HttpStatusCode.BadRequest)
            }
            val familyId = call.parameters["familyId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val choreId = call.parameters["choreId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

            val fileId = "$familyId/$choreId/${fileIdParams.joinToString("/")}"

            val inputStream = fileStorage.getFile(fileId)
            if (inputStream != null) {
                call.respondOutputStream {
                    inputStream.use { input ->
                        input.copyTo(this)
                    }
                }
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
