package com.example.manekelsa.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manekelsa.domain.model.WorkerProfile
import com.example.manekelsa.domain.usecase.CallWorkerUseCase
import com.example.manekelsa.domain.usecase.GetDeviceIdUseCase
import com.example.manekelsa.domain.usecase.GiveThumbsUpUseCase
import com.example.manekelsa.domain.usecase.ObserveWorkerProfileUseCase
import com.example.manekelsa.presentation.navigation.Screen
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

data class WorkerDetailsUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val profile: WorkerProfile? = null,
    val canCall: Boolean = false,
    val dialPhoneNumber: String? = null,
    val hasRated: Boolean = false,
    val isGivingThumbsUp: Boolean = false,
)

@HiltViewModel
class WorkerDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeWorkerProfileUseCase: ObserveWorkerProfileUseCase,
    private val giveThumbsUpUseCase: GiveThumbsUpUseCase,
    private val getDeviceIdUseCase: GetDeviceIdUseCase,
    private val callWorkerUseCase: CallWorkerUseCase,
) : ViewModel() {
    private val workerId: String = checkNotNull(savedStateHandle[Screen.WorkerDetails.WORKER_ID_ARG])
    private val _uiState = MutableStateFlow(WorkerDetailsUiState())
    val uiState: StateFlow<WorkerDetailsUiState> = _uiState.asStateFlow()

    private var raterId: String = ""
    private var observeJob: Job? = null

    init {
        observeProfile()
    }

    fun retry() {
        observeProfile()
    }

    fun giveThumbsUp() {
        val currentProfile = _uiState.value.profile ?: return
        if (_uiState.value.hasRated) return

        viewModelScope.launch {
            _uiState.update { it.copy(isGivingThumbsUp = true, hasError = false) }
            giveThumbsUpUseCase(currentProfile.id, raterId)
                .onSuccess {
                    val updatedRatedBy = currentProfile.ratedBy + raterId
                    _uiState.update {
                        it.copy(
                            isGivingThumbsUp = false,
                            hasRated = true,
                            profile = currentProfile.copy(
                                ratedBy = updatedRatedBy,
                                thumbsUpCount = maxOf(
                                    currentProfile.thumbsUpCount + 1,
                                    updatedRatedBy.size,
                                ),
                                updatedAt = System.currentTimeMillis(),
                            ),
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isGivingThumbsUp = false,
                            hasError = true,
                        )
                    }
                }
        }
    }

    private fun observeProfile() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            raterId = getDeviceIdUseCase()
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
                            val dialPhone = profile?.let { callWorkerUseCase(it.phoneNumber).getOrNull() }
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    hasError = false,
                                    profile = profile,
                                    canCall = dialPhone != null,
                                    dialPhoneNumber = dialPhone,
                                    hasRated = profile?.ratedBy?.contains(raterId) ?: false,
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
