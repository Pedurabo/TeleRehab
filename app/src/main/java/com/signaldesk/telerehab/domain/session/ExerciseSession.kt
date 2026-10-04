package com.signaldesk.telerehab.domain.session

import java.time.Instant

data class ExerciseSession(
    val id: String,
    val assignmentId: String,
    val patientId: String,
    val startedAt: Instant,
    val completedAt: Instant?,
    val status: ExerciseSessionStatus,
    val syncStatus: SyncStatus,
)
