package com.signaldesk.telerehab.domain.session.usecase

import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionRepository
import javax.inject.Inject

class GetRecentCompletedSessions @Inject constructor(
    private val repository: ExerciseSessionRepository,
) {

    suspend operator fun invoke(
        patientId: String,
        limit: Int,
    ): List<ExerciseSession> {
        require(patientId.isNotBlank()) {
            "patientId must not be blank"
        }

        require(limit > 0) {
            "limit must be greater than zero"
        }

        return repository.findRecentCompletedByPatient(
            patientId = patientId,
            limit = limit,
        )
    }
}
