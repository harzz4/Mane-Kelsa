package com.example.manekelsa.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manekelsa.domain.model.WorkerProfile
import com.example.manekelsa.domain.usecase.GetWorkerIdUseCase
import com.example.manekelsa.domain.usecase.ObserveWorkerProfileUseCase
import com.example.manekelsa.domain.usecase.UpdateAvailabilityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkerHomeUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val profile: WorkerProfile? = null,
    val isUpdatingAvailability: Boolean = false,
)

@HiltViewModel
class WorkerHomeViewModel @Inject constructor(
    private val getWorkerIdUseCase: GetWorkerIdUseCase,
    private val observeWorkerProfileUseCase: ObserveWorkerProfileUseCase,
    private val updateAvailabilityUseCase: UpdateAvailabilityUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkerHomeUiState())
    val uiState: StateFlow<WorkerHomeUiState> = _uiState.asStateFlow()

    private var workerId: String = ""
    private var observeJob: Job? = null

    init {
        observeProfile()
    }

    fun retry() {
        observeProfile()
    }

    fun updateAvailability(isAvailableToday: Boolean) {
        val currentProfile = _uiState.value.profile ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingAvailability = true, hasError = false) }
            updateAvailabilityUseCase(currentProfile.id, isAvailableToday)
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isUpdatingAvailability = false,
                            hasError = true,
                        )
                    }
                }
                .onSuccess {
                    _uiState.update { it.copy(isUpdatingAvailability = false) }
                }
        }
    }

    private fun observeProfile() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            workerId = getWorkerIdUseCase()
            observeWorkerProfileUseCase(workerId)
                .onStart {
                    _uiState.update {
                        it.copy(
                            isLoading = true,
                            hasError = false,
                        )
                    }
                }
                .catch {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasError = true,
                        )
                    }
                }
                .collect { result ->
                    result
                        .onSuccess { profile ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    hasError = false,
                                    profile = profile,
                                )
                            }
                        }
                        .onFailure {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    hasError = true,
                                )
                            }
                        }
                }
        }
    }
}
