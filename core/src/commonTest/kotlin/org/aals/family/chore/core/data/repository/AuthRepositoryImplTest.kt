package org.aals.family.chore.core.data.repository

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import co.touchlab.kermit.Logger
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.aals.family.chore.core.data.remote.BaseUrlProvider
import org.aals.family.chore.core.data.remote.HttpClientFactory
import org.aals.family.chore.core.data.remote.PairingDataSource
import org.aals.family.chore.core.data.remote.dto.CreateFamilyResponse
import org.aals.family.chore.core.data.remote.dto.PairingUsersResponse
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.util.Result
import kotlin.test.BeforeTest
import kotlin.test.Test

class AuthRepositoryImplTest {

    private lateinit var repository: AuthRepositoryImpl
    private lateinit var tokenStorage: FakeTokenStorage
    private val serverUrl = "http://localhost:8080"
    private val baseUrlProvider = object : BaseUrlProvider {
        override suspend fun getBaseUrl(): String = serverUrl
    }

    @BeforeTest
    fun setUp() {
        tokenStorage = FakeTokenStorage()
    }

    private fun createRepository(engine: MockEngine): AuthRepositoryImpl {
        val httpClient = HttpClientFactory.create(engine, baseUrlProvider, Logger.withTag("Test"))
        val dataSource = PairingDataSource(httpClient)
        return AuthRepositoryImpl(dataSource, tokenStorage, Logger.withTag("Test"))
    }

    @Test
    fun `createFamily saves familyId and token on success`() = runTest {
        val expectedResponse = CreateFamilyResponse(
            familyId = "family123",
            parentUser = User("user123", "family123", "Dad", UserRole.PARENT, 0),
            token = "jwt_token"
        )
        val engine = MockEngine { request ->
            respond(
                content = Json.encodeToString(expectedResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        repository = createRepository(engine)
        tokenStorage.saveServerUrl(serverUrl)

        val result = repository.createFamily("The Smith", "Dad")

        assertThat(result).isInstanceOf(Result.Success::class)
        val successResult = result as Result.Success
        assertThat(successResult.data.nickname).isEqualTo("Dad")
        assertThat(tokenStorage.getFamilyId()).isEqualTo("family123")
        assertThat(tokenStorage.getToken()).isEqualTo("jwt_token")
    }

    @Test
    fun `createFamily returns error on server error`() = runTest {
        val engine = MockEngine { 
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError
            )
        }
        repository = createRepository(engine)

        val result = repository.createFamily("The Smith", "Dad")

        assertThat(result).isInstanceOf(Result.Error::class)
    }

    @Test
    fun `generatePairingToken returns token on success`() = runTest {
        val engine = MockEngine { 
            respond(
                content = "{\"pairingToken\": \"token123\"}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        repository = createRepository(engine)
        tokenStorage.saveServerUrl(serverUrl)

        val result = repository.generatePairingToken("family123")

        assertThat(result).isInstanceOf(Result.Success::class)
        assertThat((result as Result.Success).data).isEqualTo("token123")
    }

    @Test
    fun `confirmPairing saves familyId and token on success`() = runTest {
        val user = User("user123", "family123", "Kid", UserRole.CHILD, 10)
        val engine = MockEngine { 
            respond(
                content = Json.encodeToString(
                    org.aals.family.chore.core.data.remote.dto.ConfirmPairingResponse("family123", user, "token")
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        repository = createRepository(engine)
        tokenStorage.saveServerUrl(serverUrl)

        val result = repository.confirmPairing("pairing_token", "user123")

        assertThat(result).isInstanceOf(Result.Success::class)
        assertThat(tokenStorage.getFamilyId()).isEqualTo("family123")
        assertThat(tokenStorage.getToken()).isEqualTo("token")
    }

    @Test
    fun `getFamilyMembers returns users on success`() = runTest {
        val users = listOf(User("1", "family123", "User", UserRole.CHILD, 0))
        val response = PairingUsersResponse("The Smiths", users)
        val engine = MockEngine { 
            respond(
                content = Json.encodeToString(response),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        repository = createRepository(engine)
        tokenStorage.saveServerUrl(serverUrl)

        val result = repository.getFamilyMembers("family123")

        assertThat(result).isInstanceOf(Result.Success::class)
        assertThat((result as Result.Success).data).isEqualTo(users)
    }
}
