package org.aals.family.chore.core.data.remote

import io.ktor.client.HttpClient
import org.aals.family.chore.core.data.remote.dto.ConfirmPairingRequest
import org.aals.family.chore.core.data.remote.dto.ConfirmPairingResponse
import org.aals.family.chore.core.data.remote.dto.CreateFamilyRequest
import org.aals.family.chore.core.data.remote.dto.CreateFamilyResponse
import org.aals.family.chore.core.data.remote.dto.GeneratePairingTokenRequest
import org.aals.family.chore.core.data.remote.dto.GeneratePairingTokenResponse
import org.aals.family.chore.core.data.remote.dto.PairingUsersResponse
import org.aals.family.chore.core.data.remote.dto.SetupPinRequest
import org.aals.family.chore.core.data.remote.dto.VerifyPinRequest
import org.aals.family.chore.core.domain.model.Family
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result

class PairingDataSource(
    private val httpClient: HttpClient
) {
    suspend fun createFamily(
        familyName: String,
        parentNickname: String
    ): Result<CreateFamilyResponse, DataError.Network> {
        return httpClient.post(
            route = "auth/family/create",
            body = CreateFamilyRequest(familyName, parentNickname)
        )
    }

    suspend fun getFamilies(): Result<List<Family>, DataError.Network> {
        return httpClient.get(
            route = "auth/families"
        )
    }

    suspend fun generatePairingToken(
        familyId: String
    ): Result<GeneratePairingTokenResponse, DataError.Network> {
        return httpClient.post(
            route = "auth/pair/generate",
            body = GeneratePairingTokenRequest(familyId)
        )
    }

    suspend fun getPairingUsers(
        pairingToken: String
    ): Result<PairingUsersResponse, DataError.Network> {
        return httpClient.get(
            route = "auth/pair/users",
            queryParameters = mapOf("token" to pairingToken)
        )
    }

    suspend fun getFamilyMembers(
        familyId: String
    ): Result<PairingUsersResponse, DataError.Network> {
        return httpClient.get(
            route = "auth/family/$familyId/users"
        )
    }

    suspend fun confirmPairing(
        pairingToken: String,
        userId: String
    ): Result<ConfirmPairingResponse, DataError.Network> {
        return httpClient.post(
            route = "auth/pair/confirm",
            body = ConfirmPairingRequest(pairingToken, userId)
        )
    }

    suspend fun setupPin(
        userId: String,
        pin: String
    ): Result<Unit, DataError.Network> {
        return httpClient.post<SetupPinRequest, Unit>(
            route = "auth/pin/setup",
            body = SetupPinRequest(userId, pin)
        )
    }

    suspend fun verifyPin(
        userId: String,
        pin: String
    ): Result<Unit, DataError.Network> {
        return httpClient.post<VerifyPinRequest, Unit>(
            route = "auth/pin/verify",
            body = VerifyPinRequest(userId, pin)
        )
    }

    suspend fun getUser(userId: String): Result<User, DataError.Network> {
        return httpClient.get(
            route = "auth/user/$userId"
        )
    }
}
