package com.example.manekelsa.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manekelsa.domain.model.UserRole
import com.example.manekelsa.domain.usecase.GetUserRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SplashUiState(
    val isLoading: Boolean = true,
    val role: UserRole? = null,
    val isReady: Boolean = false,
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getUserRoleUseCase: GetUserRoleUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedRole = getUserRoleUseCase().first()
            _uiState.value = SplashUiState(
                isLoading = false,
                role = savedRole,
                isReady = true,
            )
        }
    }
}
