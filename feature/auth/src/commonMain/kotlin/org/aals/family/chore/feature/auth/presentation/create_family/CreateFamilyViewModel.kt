package org.aals.family.chore.feature.auth.presentation.create_family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.repository.AuthRepository
import org.aals.family.chore.core.domain.util.onFailure
import org.aals.family.chore.core.domain.util.onSuccess

class CreateFamilyViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateFamilyState())
    val state = _state.asStateFlow()

    private val _events = Channel<CreateFamilyEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CreateFamilyAction) {
        when (action) {
            is CreateFamilyAction.OnFamilyNameChange -> {
                _state.update { it.copy(familyName = action.name) }
            }
            is CreateFamilyAction.OnParentNicknameChange -> {
                _state.update { it.copy(parentNickname = action.nickname) }
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
        val familyName = _state.value.familyName
        val parentNickname = _state.value.parentNickname

        if (familyName.isBlank() || parentNickname.isBlank()) {
            _state.update { it.copy(error = "Please fill all fields") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
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
