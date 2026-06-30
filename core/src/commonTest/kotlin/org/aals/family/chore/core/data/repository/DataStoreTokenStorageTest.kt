package org.aals.family.chore.core.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreTokenStorageTest {

    private lateinit var tokenStorage: DataStoreTokenStorage
    private lateinit var testScope: CoroutineScope
    private lateinit var testPath: okio.Path

    @BeforeTest
    fun setUp() {
        testScope = CoroutineScope(UnconfinedTestDispatcher() + Job())
        // Use a unique path in the current directory for the test
        testPath = "test_${kotlin.random.Random.nextInt()}.preferences_pb".toPath()
        
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { testPath },
            scope = testScope
        )
        tokenStorage = DataStoreTokenStorage(dataStore)
    }

    @AfterTest
    fun tearDown() {
        testScope.cancel()
        // We don't strictly need to delete it here if we use unique names,
        // but it's good practice if we can resolve FileSystem.SYSTEM
    }

    @Test
    fun `save and get token`() = runTest {
        tokenStorage.saveToken("test_token")
        assertThat(tokenStorage.getToken()).isEqualTo("test_token")
    }

    @Test
    fun `save and get family id`() = runTest {
        tokenStorage.saveFamilyId("family_123")
        assertThat(tokenStorage.getFamilyId()).isEqualTo("family_123")
    }

    @Test
    fun `save and get server url`() = runTest {
        tokenStorage.saveServerUrl("http://10.0.2.2:8080")
        assertThat(tokenStorage.getServerUrl()).isEqualTo("http://10.0.2.2:8080")
    }

    @Test
    fun `save and get server name`() = runTest {
        tokenStorage.saveServerName("Main Server")
        assertThat(tokenStorage.getServerName()).isEqualTo("Main Server")
    }

    @Test
    fun `clear storage`() = runTest {
        tokenStorage.saveToken("token")
        tokenStorage.saveFamilyId("id")
        tokenStorage.clear()
        
        assertThat(tokenStorage.getToken()).isNull()
        assertThat(tokenStorage.getFamilyId()).isNull()
    }
}
