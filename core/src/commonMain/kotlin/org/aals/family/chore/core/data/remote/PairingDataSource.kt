package org.aals.family.chore.core.data.remote

import io.ktor.client.HttpClient
import org.aals.family.chore.core.data.remote.dto.*
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.util.DataError
import org.aals.family.chore.core.domain.util.Result

class PairingDataSource(
    private val httpClient: HttpClient
) {
    suspend fun createFamily(
        serverUrl: String,
        familyName: String,
        parentNickname: String
    ): Result<CreateFamilyResponse, DataError.Network> {
        return httpClient.post(
            route = "$serverUrl/auth/family/create",
            body = CreateFamilyRequest(familyName, parentNickname)
        )
    }

    suspend fun generatePairingToken(
        serverUrl: String,
        familyId: String
    ): Result<GeneratePairingTokenResponse, DataError.Network> {
        return httpClient.post(
            route = "$serverUrl/auth/pair/generate",
            body = GeneratePairingTokenRequest(familyId)
        )
    }

    suspend fun getPairingUsers(
        serverUrl: String,
        pairingToken: String
    ): Result<PairingUsersResponse, DataError.Network> {
        return httpClient.get(
            route = "$serverUrl/auth/pair/users",
            queryParameters = mapOf("token" to pairingToken)
        )
    }

    suspend fun getFamilyUsers(
        serverUrl: String,
        familyId: String
    ): Result<PairingUsersResponse, DataError.Network> {
        return httpClient.get(
            route = "$serverUrl/auth/family/$familyId/users"
        )
    }

    suspend fun confirmPairing(
        serverUrl: String,
        pairingToken: String,
        userId: String
    ): Result<ConfirmPairingResponse, DataError.Network> {
        return httpClient.post(
            route = "$serverUrl/auth/pair/confirm",
            body = ConfirmPairingRequest(pairingToken, userId)
        )
    }

    suspend fun setupPin(
        serverUrl: String,
        userId: String,
        pin: String
    ): Result<Unit, DataError.Network> {
        return httpClient.post<SetupPinRequest, Unit>(
            route = "$serverUrl/auth/pin/setup",
            body = SetupPinRequest(userId, pin)
        )
    }

    suspend fun verifyPin(
        serverUrl: String,
        userId: String,
        pin: String
    ): Result<Unit, DataError.Network> {
        return httpClient.post<VerifyPinRequest, Unit>(
            route = "$serverUrl/auth/pin/verify",
            body = VerifyPinRequest(userId, pin)
        )
    }
}
