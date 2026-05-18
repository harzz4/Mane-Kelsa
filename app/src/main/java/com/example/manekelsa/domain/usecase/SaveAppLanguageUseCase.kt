package com.example.manekelsa.domain.usecase

import com.example.manekelsa.domain.model.AppLanguage
import com.example.manekelsa.domain.repository.UserPreferencesRepository

class SaveAppLanguageUseCase(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(language: AppLanguage) {
        repository.saveAppLanguage(language)
    }
}
