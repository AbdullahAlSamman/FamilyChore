package org.aals.family.chore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.aals.family.chore.core.domain.repository.ConnectivityRepository
import org.aals.family.chore.core.domain.repository.TokenStorage
import org.aals.family.chore.core.domain.util.Result
import org.aals.family.chore.feature.auth.presentation.navigation.ServerDiscoveryRoute
import org.aals.family.chore.feature.auth.presentation.navigation.UserSelectionRoute
import org.aals.family.chore.feature.auth.presentation.navigation.WelcomeRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.DashboardGraph

class MainViewModel(
    private val tokenStorage: TokenStorage,
    private val connectivityRepository: ConnectivityRepository
) : ViewModel() {

    private val _state = MutableStateFlow<MainState>(MainState.Loading)
    val state = _state.asStateFlow()

    init {
        checkInitialState()
    }

    private fun checkInitialState() {
        viewModelScope.launch {
            val serverUrl = tokenStorage.getServerUrl()
            val familyId = tokenStorage.getFamilyId()
            val token = tokenStorage.getToken()

            val destination = if (token != null) {
                DashboardGraph
            } else if (serverUrl != null) {
                val result = connectivityRepository.checkHealth()
                if (result is Result.Success) {
                    if (familyId != null) {
                        UserSelectionRoute(familyId = familyId)
                    } else {
                        WelcomeRoute
                    }
                } else {
                    ServerDiscoveryRoute(isErrorMode = true)
                }
            } else {
                ServerDiscoveryRoute()
            }

            _state.value = MainState.Success(destination)
        }
    }
}
