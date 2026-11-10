package org.aals.family.chore.core.data.remote

import io.ktor.client.HttpClient
import org.aals.family.chore.core.data.remote.dto.ChoreDto
import org.aals.family.chore.core.data.remote.dto.ChoresResponse
import org.aals.family.chore.core.data.remote.dto.CreateChoreRequest
import org.aals.family.chore.core.data.remote.dto.UpdateChoreStatusRequest
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.core.domain.util.map

import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

open class ChoreDataSource(
    private val httpClient: HttpClient,
    private val baseUrlProvider: BaseUrlProvider
) {
    open suspend fun uploadPhotoProof(
        familyId: String,
        choreId: String,
        fileName: String,
        photoBytes: ByteArray
    ): Result<String, DataError.Network> {
        return safeCall<org.aals.family.chore.core.data.remote.dto.FileUploadResponseDto> {
            httpClient.post("family/$familyId/chores/$choreId/proof") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", photoBytes, Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            })
                        }
                    )
                )
            }
        }.map { it.fileId }
    }
    open fun observeChoreSync(familyId: String): Flow<ChoreDto> = flow {
        val baseUrl = baseUrlProvider.getBaseUrl() ?: return@flow
        val wsUrl = baseUrl.replace("http://", "ws://").replace("https://", "wss://").trimEnd('/') + "/family/$familyId/chores/ws"
        httpClient.webSocket(wsUrl) {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try {
                        val choreDto = Json.decodeFromString<ChoreDto>(text)
                        emit(choreDto)
                    } catch (e: Exception) {
                        // ignore malformed messages
                    }
                }
            }
        }
    }
    open suspend fun getChores(
        familyId: String,
        userId: String? = null
    ): Result<List<ChoreDto>, DataError.Network> {
        return httpClient.get<ChoresResponse>(
            route = "family/$familyId/chores",
            queryParameters = buildMap {
                userId?.let { put("userId", it) }
            }
        ).map { it.chores }
    }

    open suspend fun createChore(
        request: CreateChoreRequest
    ): Result<ChoreDto, DataError.Network> {
        return httpClient.post(
            route = "family/${request.familyId}/chores",
            body = request
        )
    }

    open suspend fun updateChoreStatus(
        request: UpdateChoreStatusRequest
    ): Result<Unit, DataError.Network> {
        return httpClient.patch(
            route = "family/${request.familyId}/chores/${request.choreId}/status",
            body = request
        )
    }

    open suspend fun uploadTaskImage(
        familyId: String,
        choreId: String,
        fileName: String,
        imageBytes: ByteArray
    ): Result<Unit, DataError.Network> {
        return safeCall<Unit> {
            httpClient.post("family/$familyId/chores/$choreId/image") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", imageBytes, Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            })
                        }
                    )
                )
            }
        }
    }

    open suspend fun downloadTaskImage(
        familyId: String,
        choreId: String
    ): Result<ByteArray, DataError.Network> {
        return httpClient.get<ByteArray>("family/$familyId/chores/$choreId/image")
    }
}
