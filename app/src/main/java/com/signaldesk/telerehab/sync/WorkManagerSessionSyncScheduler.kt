package com.signaldesk.telerehab.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.signaldesk.telerehab.domain.session.sync.SessionSyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerSessionSyncScheduler @Inject constructor(
    @ApplicationContext context: Context,
) : SessionSyncScheduler {

    private val workManager =
        WorkManager.getInstance(context)

    override fun requestSync() {
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val request =
            OneTimeWorkRequestBuilder<SyncPendingSessionsWorker>()
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request,
        )
    }

    private companion object {
        const val UNIQUE_WORK_NAME =
            "teleRehabExerciseSessionSync"
    }
}
