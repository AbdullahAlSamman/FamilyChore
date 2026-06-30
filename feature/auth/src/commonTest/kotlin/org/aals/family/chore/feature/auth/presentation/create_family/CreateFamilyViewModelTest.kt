package org.aals.family.chore.feature.auth.presentation.create_family

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.aals.family.chore.feature.auth.presentation.FakeAuthRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateFamilyViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: CreateFamilyViewModel
    private lateinit var authRepository: FakeAuthRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        viewModel = CreateFamilyViewModel(authRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `creating family with valid data sends event`() = runTest {
        viewModel.onAction(CreateFamilyAction.OnFamilyNameChange("The Smith"))
        viewModel.onAction(CreateFamilyAction.OnParentNicknameChange("Dad"))
        
        viewModel.events.test {
            viewModel.onAction(CreateFamilyAction.OnCreateClick)
            val event = awaitItem() as CreateFamilyEvent.FamilyCreated
            assertThat(event.familyId).isEqualTo("family1")
            assertThat(event.userId).isEqualTo("1")
        }
    }

    @Test
    fun `creating family with empty fields sets error`() = runTest {
        viewModel.onAction(CreateFamilyAction.OnCreateClick)
        
        viewModel.state.test {
            assertThat(awaitItem().error).isEqualTo("Please fill all fields")
        }
    }
}
