package com.example.manekelsa.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manekelsa.domain.model.AppLanguage
import com.example.manekelsa.domain.model.UserRole
import com.example.manekelsa.domain.usecase.GetAppLanguageUseCase
import com.example.manekelsa.domain.usecase.SaveAppLanguageUseCase
import com.example.manekelsa.domain.usecase.SaveUserRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoleSelectionUiState(
    val isSaving: Boolean = false,
    val savedRole: UserRole? = null,
    val hasError: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.DEFAULT,
)

@HiltViewModel
class RoleSelectionViewModel @Inject constructor(
    private val saveUserRoleUseCase: SaveUserRoleUseCase,
    private val getAppLanguageUseCase: GetAppLanguageUseCase,
    private val saveAppLanguageUseCase: SaveAppLanguageUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RoleSelectionUiState())
    val uiState: StateFlow<RoleSelectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAppLanguageUseCase().collect { language ->
                _uiState.update { it.copy(appLanguage = language) }
            }
        }
    }

    fun selectRole(role: UserRole) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, hasError = false) }
            runCatching { saveUserRoleUseCase(role) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            savedRole = role,
                            hasError = false,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            savedRole = null,
                            hasError = true,
                        )
                    }
                }
        }
    }

    fun changeLanguage(language: AppLanguage) {
        viewModelScope.launch {
            saveAppLanguageUseCase(language)
        }
    }
}
