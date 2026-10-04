package com.signaldesk.telerehab.domain.session.sync

import com.signaldesk.telerehab.domain.session.ExerciseSession

interface PendingExerciseSessionSource {

    suspend fun findPendingSessions(
        limit: Int,
    ): List<ExerciseSession>
}
