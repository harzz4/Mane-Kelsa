package com.example.manekelsa.domain.usecase

import com.example.manekelsa.domain.model.AppLanguage
import com.example.manekelsa.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetAppLanguageUseCase(
    private val repository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<AppLanguage> {
        return repository.appLanguage
    }
}
