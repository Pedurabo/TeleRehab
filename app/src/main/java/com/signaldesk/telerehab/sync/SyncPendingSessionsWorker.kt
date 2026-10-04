package com.signaldesk.telerehab.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.signaldesk.telerehab.domain.session.sync.SyncBatchResult
import com.signaldesk.telerehab.domain.session.sync.SyncPendingSessions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncPendingSessionsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val syncPendingSessions: SyncPendingSessions,
) : CoroutineWorker(
    appContext,
    workerParameters,
) {

    override suspend fun doWork(): Result =
        when (syncPendingSessions()) {
            SyncBatchResult.COMPLETED -> Result.success()
            SyncBatchResult.RETRY -> Result.retry()
        }
}
