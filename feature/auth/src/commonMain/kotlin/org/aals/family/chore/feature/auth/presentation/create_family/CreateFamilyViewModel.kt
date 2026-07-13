package org.aals.family.chore.feature.auth.presentation.create_family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.util.onFailure
import org.aals.family.chore.core.domain.util.onSuccess
import org.aals.family.chore.core.domain.validation.AuthValidator
import org.aals.family.chore.core.presentation.toUiText

class CreateFamilyViewModel(
    private val authRepository: AuthRepository,
    private val logger: Logger
) : ViewModel() {

    private val _state = MutableStateFlow(CreateFamilyState())
    val state = _state.asStateFlow()

    private val _events = Channel<CreateFamilyEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CreateFamilyAction) {
        when (action) {
            is CreateFamilyAction.OnFamilyNameChange -> {
                _state.update { it.copy(familyName = action.name, familyNameError = null) }
            }
            is CreateFamilyAction.OnParentNicknameChange -> {
                _state.update { it.copy(parentNickname = action.nickname, nicknameError = null) }
            }
            CreateFamilyAction.OnCreateClick -> createFamily()
            CreateFamilyAction.OnBackClick -> {
                viewModelScope.launch {
                    _events.send(CreateFamilyEvent.NavigateBack)
                }
            }
        }
    }

    private fun createFamily() {
        val familyName = _state.value.familyName.trim()
        val parentNickname = _state.value.parentNickname.trim()

        val familyNameError = AuthValidator.validateFamilyName(familyName)
        val nicknameError = AuthValidator.validateNickname(parentNickname)

        if (familyNameError != null || nicknameError != null) {
            _state.update { 
                it.copy(
                    familyNameError = familyNameError?.toUiText(),
                    nicknameError = nicknameError?.toUiText()
                )
            }
            return
        }

        logger.d { "Creating family: $familyName (Parent: $parentNickname)" }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.createFamily(familyName, parentNickname)
                .onSuccess { user ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(CreateFamilyEvent.FamilyCreated(user.familyId, user.id))
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toString()) }
                }
        }
    }
}
