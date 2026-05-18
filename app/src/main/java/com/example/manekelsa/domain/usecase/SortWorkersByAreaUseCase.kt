package com.example.manekelsa.domain.usecase

import com.example.manekelsa.domain.model.WorkerProfile

class SortWorkersByAreaUseCase {
    operator fun invoke(workers: List<WorkerProfile>, areaQuery: String): List<WorkerProfile> {
        val query = areaQuery.normalizedLocation()
        return workers.sortedWith(
            compareByDescending<WorkerProfile> { worker -> nearestLocationScore(worker, query) }
                .thenByDescending { it.isAvailableToday }
                .thenByDescending { it.thumbsUpCount }
                .thenByDescending { it.updatedAt }
                .thenBy { it.area.normalizedLocation() }
                .thenBy { it.street.orEmpty().normalizedLocation() }
                .thenBy { it.name.normalizedLocation() },
        )
    }

    private fun nearestLocationScore(worker: WorkerProfile, query: String): Int {
        if (query.isBlank()) return 0

        val street = worker.street.orEmpty().normalizedLocation()
        val area = worker.area.normalizedLocation()
        val combined = listOf(street, area)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .normalizedLocation()

        val streetScore = locationScore(street, query, exactWeight = 1_000)
        val areaScore = locationScore(area, query, exactWeight = 800)
        val combinedScore = locationScore(combined, query, exactWeight = 600)

        return maxOf(streetScore, areaScore, combinedScore)
    }

    private fun locationScore(
        location: String,
        query: String,
        exactWeight: Int,
    ): Int {
        if (location.isBlank()) return 0

        return when {
            location == query -> exactWeight
            location.startsWith(query) -> exactWeight - 80
            query.startsWith(location) -> exactWeight - 120
            location.contains(query) -> exactWeight - 180
            query.contains(location) -> exactWeight - 220
            else -> wordOverlapScore(location, query)
        }
    }

    private fun wordOverlapScore(location: String, query: String): Int {
        val locationWords = location.split(" ").filter { it.isNotBlank() }.toSet()
        val queryWords = query.split(" ").filter { it.isNotBlank() }.toSet()
        if (locationWords.isEmpty() || queryWords.isEmpty()) return 0

        val overlap = locationWords.intersect(queryWords).size
        val streetOrAreaCloseness = overlap * 100
        val coverageBonus = (overlap * 100) / queryWords.size
        return streetOrAreaCloseness + coverageBonus
    }

    private fun String.normalizedLocation(): String {
        return trim()
            .lowercase()
            .replace(Regex("[^a-z0-9\\p{L}\\p{Nd}]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
