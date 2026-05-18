package com.example.manekelsa.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manekelsa.domain.model.AppLanguage
import com.example.manekelsa.domain.model.UserRole
import com.example.manekelsa.domain.usecase.ClearUserRoleUseCase
import com.example.manekelsa.domain.usecase.GetAppLanguageUseCase
import com.example.manekelsa.domain.usecase.GetUserRoleUseCase
import com.example.manekelsa.domain.usecase.SaveAppLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val role: UserRole? = null,
    val appLanguage: AppLanguage = AppLanguage.DEFAULT,
    val resetComplete: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserRoleUseCase: GetUserRoleUseCase,
    private val clearUserRoleUseCase: ClearUserRoleUseCase,
    private val getAppLanguageUseCase: GetAppLanguageUseCase,
    private val saveAppLanguageUseCase: SaveAppLanguageUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                getUserRoleUseCase(),
                getAppLanguageUseCase(),
            ) { role, language -> role to language }
                .collect { (role, language) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            role = role,
                            appLanguage = language,
                        )
                    }
                }
        }
    }

    fun resetRole() {
        viewModelScope.launch {
            clearUserRoleUseCase()
            _uiState.update { it.copy(resetComplete = true) }
        }
    }

    fun changeLanguage(language: AppLanguage) {
        viewModelScope.launch {
            saveAppLanguageUseCase(language)
        }
    }
}
