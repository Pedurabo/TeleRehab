package com.signaldesk.telerehab.domain.session.sync

import com.signaldesk.telerehab.domain.session.ExerciseSessionRepository
import com.signaldesk.telerehab.domain.session.SyncStatus
import javax.inject.Inject

class SyncPendingSessions @Inject constructor(
    private val pendingSource: PendingExerciseSessionSource,
    private val repository: ExerciseSessionRepository,
    private val gateway: ExerciseSessionSyncGateway,
) {

    suspend operator fun invoke(
        limit: Int = DEFAULT_BATCH_SIZE,
    ): SyncBatchResult {
        require(limit > 0) {
            "limit must be greater than zero"
        }

        val pendingSessions =
            pendingSource.findPendingSessions(limit)

        for (session in pendingSessions) {
            when (gateway.upsertSession(session)) {
                SessionSyncResult.SUCCESS -> {
                    repository.save(
                        session.copy(
                            syncStatus = SyncStatus.SYNCED,
                        ),
                    )
                }

                SessionSyncResult.RETRYABLE_FAILURE -> {
                    return SyncBatchResult.RETRY
                }

                SessionSyncResult.PERMANENT_FAILURE -> {
                    repository.save(
                        session.copy(
                            syncStatus = SyncStatus.FAILED,
                        ),
                    )
                }
            }
        }

        return SyncBatchResult.COMPLETED
    }

    companion object {
        const val DEFAULT_BATCH_SIZE = 20
    }
}
