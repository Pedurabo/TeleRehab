package com.signaldesk.telerehab.domain.session.sync

import com.signaldesk.telerehab.domain.session.ExerciseSession

interface ExerciseSessionSyncGateway {

    suspend fun upsertSession(
        session: ExerciseSession,
    ): SessionSyncResult
}
