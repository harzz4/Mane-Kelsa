package com.example.manekelsa.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manekelsa.domain.model.ServiceType
import com.example.manekelsa.domain.model.WorkerProfile
import com.example.manekelsa.domain.usecase.CallWorkerUseCase
import com.example.manekelsa.domain.usecase.GenerateWorkerDescriptionUseCase
import com.example.manekelsa.domain.usecase.GetWorkerIdUseCase
import com.example.manekelsa.domain.usecase.ObserveWorkerProfileUseCase
import com.example.manekelsa.domain.usecase.SaveWorkerProfileUseCase
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

data class WorkerProfileEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val hasError: Boolean = false,
    val isSaved: Boolean = false,
    val workerId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val selectedServiceType: ServiceType? = null,
    val area: String = "",
    val street: String = "",
    val dailyRate: String = "",
    val twoHourRate: String = "",
    val experienceYears: String = "",
    val description: String = "",
    val nameError: Boolean = false,
    val phoneError: Boolean = false,
    val areaError: Boolean = false,
    val serviceError: Boolean = false,
    val dailyRateError: Boolean = false,
    val twoHourRateError: Boolean = false,
)

@HiltViewModel
class WorkerProfileEditViewModel @Inject constructor(
    private val getWorkerIdUseCase: GetWorkerIdUseCase,
    private val observeWorkerProfileUseCase: ObserveWorkerProfileUseCase,
    private val saveWorkerProfileUseCase: SaveWorkerProfileUseCase,
    private val callWorkerUseCase: CallWorkerUseCase,
    private val generateWorkerDescriptionUseCase: GenerateWorkerDescriptionUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkerProfileEditUiState())
    val uiState: StateFlow<WorkerProfileEditUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var userEdited = false
    private var loadedProfile: WorkerProfile? = null

    init {
        observeProfile()
    }

    fun retry() {
        observeProfile()
    }

    fun onNameChange(value: String) {
        markEdited()
        _uiState.update { it.copy(name = value, nameError = false, isSaved = false) }
    }

    fun onPhoneNumberChange(value: String) {
        markEdited()
        _uiState.update { it.copy(phoneNumber = value, phoneError = false, isSaved = false) }
    }

    fun onPhotoUrlChange(value: String) {
        markEdited()
        _uiState.update { it.copy(photoUrl = value, isSaved = false) }
    }

    fun onServiceSelected(serviceType: ServiceType) {
        markEdited()
        _uiState.update {
            it.copy(
                selectedServiceType = serviceType,
                serviceError = false,
                isSaved = false,
            )
        }
    }

    fun onAreaChange(value: String) {
        markEdited()
        _uiState.update { it.copy(area = value, areaError = false, isSaved = false) }
    }

    fun onStreetChange(value: String) {
        markEdited()
        _uiState.update { it.copy(street = value, isSaved = false) }
    }

    fun onDailyRateChange(value: String) {
        markEdited()
        _uiState.update {
            it.copy(
                dailyRate = value.onlyDigits(),
                dailyRateError = false,
                isSaved = false,
            )
        }
    }

    fun onTwoHourRateChange(value: String) {
        markEdited()
        _uiState.update {
            it.copy(
                twoHourRate = value.onlyDigits(),
                twoHourRateError = false,
                isSaved = false,
            )
        }
    }

    fun onExperienceYearsChange(value: String) {
        markEdited()
        _uiState.update { it.copy(experienceYears = value.onlyDigits(), isSaved = false) }
    }

    fun onDescriptionChange(value: String) {
        markEdited()
        _uiState.update { it.copy(description = value, isSaved = false) }
    }

    fun generateDescription(
        serviceName: String,
        withExperienceTemplate: String,
        withoutExperienceTemplate: String,
    ) {
        val state = _uiState.value
        val generated = generateWorkerDescriptionUseCase(
            name = state.name,
            serviceName = serviceName,
            area = state.area,
            experienceYears = state.experienceYears.toIntOrNull() ?: 0,
            withExperienceTemplate = withExperienceTemplate,
            withoutExperienceTemplate = withoutExperienceTemplate,
        )
        if (generated.isNotBlank()) {
            markEdited()
            _uiState.update { it.copy(description = generated, isSaved = false) }
        }
    }

    fun saveProfile() {
        val state = _uiState.value
        val dailyRate = state.dailyRate.toIntOrNull() ?: 0
        val twoHourRate = state.twoHourRate.toIntOrNull() ?: 0

        val nameError = state.name.isBlank()
        val phoneError = !callWorkerUseCase.isValidPhoneNumber(state.phoneNumber)
        val areaError = state.area.isBlank()
        val serviceError = state.selectedServiceType == null
        val dailyRateError = dailyRate <= 0
        val twoHourRateError = twoHourRate <= 0

        if (
            nameError ||
            phoneError ||
            areaError ||
            serviceError ||
            dailyRateError ||
            twoHourRateError
        ) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    phoneError = phoneError,
                    areaError = areaError,
                    serviceError = serviceError,
                    dailyRateError = dailyRateError,
                    twoHourRateError = twoHourRateError,
                )
            }
            Log.d("Sourik", "Error in VM")
            return
        }

        val existing = loadedProfile
        val profile = WorkerProfile(
            id = state.workerId,
            name = state.name.trim(),
            phoneNumber = state.phoneNumber.trim(),
            photoUrl = state.photoUrl.trim().ifBlank { null },
            serviceType = requireNotNull(state.selectedServiceType),
            area = state.area.trim(),
            street = state.street.trim().ifBlank { null },
            dailyRate = dailyRate,
            twoHourRate = twoHourRate,
            experienceYears = state.experienceYears.toIntOrNull() ?: 0,
            description = state.description.trim(),
            isAvailableToday = existing?.isAvailableToday ?: false,
            thumbsUpCount = existing?.thumbsUpCount ?: 0,
            ratedBy = existing?.ratedBy.orEmpty(),
            updatedAt = System.currentTimeMillis(),
        )

        viewModelScope.launch {
            Log.d("Sourik", "Call Save worker profile")
            _uiState.update { it.copy(isSaving = true, hasError = false) }
            saveWorkerProfileUseCase(profile)
                .onSuccess {
                    loadedProfile = profile
                    userEdited = false
                    Log.d("Sourik", "Call Save worker profile success")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            hasError = false,
                            isSaved = true,
                        )
                    }
                }
                .onFailure {
                    Log.d("Sourik", "Call Save worker profile error")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            hasError = true,
                        )
                    }
                }
        }
    }

    private fun observeProfile() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            val workerId = getWorkerIdUseCase()
            _uiState.update { it.copy(workerId = workerId) }

            observeWorkerProfileUseCase(workerId)
                .onStart {
                    _uiState.update { it.copy(isLoading = true, hasError = false) }
                }
                .catch {
                    _uiState.update { it.copy(isLoading = false, hasError = true) }
                }
                .collect { result ->
                    result
                        .onSuccess { profile ->
                            loadedProfile = profile
                            if (!userEdited) {
                                _uiState.update { state ->
                                    state.copy(
                                        isLoading = false,
                                        hasError = false,
                                        workerId = workerId,
                                        name = profile?.name.orEmpty(),
                                        phoneNumber = profile?.phoneNumber.orEmpty(),
                                        photoUrl = profile?.photoUrl.orEmpty(),
                                        selectedServiceType = profile?.serviceType,
                                        area = profile?.area.orEmpty(),
                                        street = profile?.street.orEmpty(),
                                        dailyRate = profile?.dailyRate?.takeIf { it > 0 }?.toString().orEmpty(),
                                        twoHourRate = profile?.twoHourRate?.takeIf { it > 0 }?.toString().orEmpty(),
                                        experienceYears = profile?.experienceYears?.takeIf { it > 0 }?.toString().orEmpty(),
                                        description = profile?.description.orEmpty(),
                                        nameError = false,
                                        phoneError = false,
                                        areaError = false,
                                        serviceError = false,
                                        dailyRateError = false,
                                        twoHourRateError = false,
                                    )
                                }
                            } else {
                                _uiState.update { it.copy(isLoading = false, hasError = false) }
                            }
                        }
                        .onFailure {
                            _uiState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    hasError = true,
                                )
                            }
                        }
                }
        }
    }

    private fun markEdited() {
        userEdited = true
    }

    private fun String.onlyDigits(): String = filter { it.isDigit() }
}
