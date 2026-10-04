package com.signaldesk.telerehab.domain.session.usecase

import com.signaldesk.telerehab.core.id.AppIdGenerator
import com.signaldesk.telerehab.core.time.AppClock
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionRepository
import com.signaldesk.telerehab.domain.session.ExerciseSessionStatus
import com.signaldesk.telerehab.domain.session.SyncStatus
import javax.inject.Inject

class StartExerciseSession @Inject constructor(
    private val repository: ExerciseSessionRepository,
    private val clock: AppClock,
    private val idGenerator: AppIdGenerator,
) {

    suspend operator fun invoke(
        assignmentId: String,
        patientId: String,
    ): ExerciseSession {
        require(assignmentId.isNotBlank()) {
            "assignmentId must not be blank"
        }

        require(patientId.isNotBlank()) {
            "patientId must not be blank"
        }

        val session =
            ExerciseSession(
                id = idGenerator.newId(),
                assignmentId = assignmentId,
                patientId = patientId,
                startedAt = clock.now(),
                completedAt = null,
                status = ExerciseSessionStatus.IN_PROGRESS,
                syncStatus = SyncStatus.PENDING,
            )

        repository.save(session)

        return session
    }
}
