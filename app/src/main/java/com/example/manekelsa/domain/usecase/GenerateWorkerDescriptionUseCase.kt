package com.example.manekelsa.domain.usecase

class GenerateWorkerDescriptionUseCase {
    operator fun invoke(
        name: String,
        serviceName: String,
        area: String,
        experienceYears: Int,
        withExperienceTemplate: String,
        withoutExperienceTemplate: String,
    ): String {
        if (name.isBlank() || serviceName.isBlank() || area.isBlank()) return ""

        return if (experienceYears > 0) {
            withExperienceTemplate.format(name, area, serviceName, experienceYears)
        } else {
            withoutExperienceTemplate.format(name, area, serviceName)
        }
    }
}
