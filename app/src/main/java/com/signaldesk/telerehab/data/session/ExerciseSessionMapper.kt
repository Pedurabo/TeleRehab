package com.signaldesk.telerehab.data.session

import com.signaldesk.telerehab.core.database.entity.ExerciseSessionEntity
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionStatus
import com.signaldesk.telerehab.domain.session.SyncStatus
import java.time.Instant

internal fun ExerciseSession.toEntity(): ExerciseSessionEntity =
    ExerciseSessionEntity(
        id = id,
        assignmentId = assignmentId,
        patientId = patientId,
        startedAtEpochMillis = startedAt.toEpochMilli(),
        completedAtEpochMillis = completedAt?.toEpochMilli(),
        sessionStatus = status.name,
        syncStatus = syncStatus.name,
    )

internal fun ExerciseSessionEntity.toDomain(): ExerciseSession =
    ExerciseSession(
        id = id,
        assignmentId = assignmentId,
        patientId = patientId,
        startedAt = Instant.ofEpochMilli(startedAtEpochMillis),
        completedAt = completedAtEpochMillis?.let(Instant::ofEpochMilli),
        status = ExerciseSessionStatus.valueOf(sessionStatus),
        syncStatus = SyncStatus.valueOf(syncStatus),
    )
