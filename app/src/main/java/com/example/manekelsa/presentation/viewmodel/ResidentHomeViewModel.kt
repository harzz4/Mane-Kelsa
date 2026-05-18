package com.example.manekelsa.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manekelsa.domain.model.ServiceType
import com.example.manekelsa.domain.model.WorkerProfile
import com.example.manekelsa.domain.usecase.ObserveWorkersUseCase
import com.example.manekelsa.domain.usecase.SortWorkersByAreaUseCase
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

data class ResidentHomeUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val areaQuery: String = "",
    val availableOnly: Boolean = true,
    val selectedServiceType: ServiceType? = null,
    val workers: List<WorkerProfile> = emptyList(),
)

@HiltViewModel
class ResidentHomeViewModel @Inject constructor(
    private val observeWorkersUseCase: ObserveWorkersUseCase,
    private val sortWorkersByAreaUseCase: SortWorkersByAreaUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ResidentHomeUiState())
    val uiState: StateFlow<ResidentHomeUiState> = _uiState.asStateFlow()

    private var allWorkers: List<WorkerProfile> = emptyList()
    private var observeJob: Job? = null

    init {
        observeWorkers()
    }

    fun retry() {
        observeWorkers()
    }

    fun onAreaQueryChange(value: String) {
        _uiState.update { it.copy(areaQuery = value) }
        rebuildVisibleWorkers()
    }

    fun onAvailableOnlyChange(availableOnly: Boolean) {
        _uiState.update { it.copy(availableOnly = availableOnly) }
        rebuildVisibleWorkers()
    }

    fun onServiceSelected(serviceType: ServiceType?) {
        _uiState.update { it.copy(selectedServiceType = serviceType) }
        rebuildVisibleWorkers()
    }

    private fun observeWorkers() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeWorkersUseCase()
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
                        .onSuccess { workers ->
                            allWorkers = workers
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    hasError = false,
                                )
                            }
                            rebuildVisibleWorkers()
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

    private fun rebuildVisibleWorkers() {
        val state = _uiState.value
        val filtered = allWorkers.filter { worker ->
            val availabilityMatches = !state.availableOnly || worker.isAvailableToday
            val serviceMatches = state.selectedServiceType == null || worker.serviceType == state.selectedServiceType
            availabilityMatches && serviceMatches
        }
        val sorted = sortWorkersByAreaUseCase(filtered, state.areaQuery)
        _uiState.update { it.copy(workers = sorted) }
    }
}
