package com.signaldesk.telerehab.data.sync

import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.sync.ExerciseSessionSyncGateway
import com.signaldesk.telerehab.domain.session.sync.SessionSyncResult
import javax.inject.Inject

class DeferredExerciseSessionSyncGateway @Inject constructor() :
    ExerciseSessionSyncGateway {

    override suspend fun upsertSession(
        session: ExerciseSession,
    ): SessionSyncResult =
        SessionSyncResult.RETRYABLE_FAILURE
}
