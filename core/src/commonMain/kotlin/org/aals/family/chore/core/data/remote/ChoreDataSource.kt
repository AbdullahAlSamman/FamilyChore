package org.aals.family.chore.core.data.remote

import io.ktor.client.HttpClient
import org.aals.family.chore.core.data.remote.dto.ChoreDto
import org.aals.family.chore.core.data.remote.dto.ChoresResponse
import org.aals.family.chore.core.data.remote.dto.CreateChoreRequest
import org.aals.family.chore.core.data.remote.dto.UpdateChoreStatusRequest
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.core.domain.util.map

open class ChoreDataSource(
    private val httpClient: HttpClient
) {
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
}
