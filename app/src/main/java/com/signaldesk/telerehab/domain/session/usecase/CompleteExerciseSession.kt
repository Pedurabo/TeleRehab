package com.signaldesk.telerehab.domain.session.usecase

import com.signaldesk.telerehab.domain.session.sync.SessionSyncScheduler

import com.signaldesk.telerehab.core.time.AppClock
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionRepository
import com.signaldesk.telerehab.domain.session.ExerciseSessionStatus
import com.signaldesk.telerehab.domain.session.SyncStatus
import javax.inject.Inject

class CompleteExerciseSession @Inject constructor(
    private val sessionSyncScheduler: SessionSyncScheduler = SessionSyncScheduler.NoOp,

    private val repository: ExerciseSessionRepository,
    private val clock: AppClock,
) {

    suspend operator fun invoke(
        sessionId: String,
    ): ExerciseSession {
        require(sessionId.isNotBlank()) {
            "sessionId must not be blank"
        }

        val existing =
            requireNotNull(
                repository.findById(sessionId),
            ) {
                "Exercise session not found: $sessionId"
            }

        check(existing.status == ExerciseSessionStatus.IN_PROGRESS) {
            "Only an in-progress exercise session can be completed"
        }

        val completionTime =
            clock.now()

        check(!completionTime.isBefore(existing.startedAt)) {
            "Exercise session cannot complete before it started"
        }

        val completed =
            existing.copy(
                completedAt = completionTime,
                status = ExerciseSessionStatus.COMPLETED,
                syncStatus = SyncStatus.PENDING,
            )

        repository.save(completed)

        sessionSyncScheduler.requestSync()

        return completed
    }
}
