package com.signaldesk.telerehab.data.session

import com.signaldesk.telerehab.core.database.dao.ExerciseSessionDao
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionRepository
import com.signaldesk.telerehab.domain.session.sync.PendingExerciseSessionSource
import javax.inject.Inject

class RoomExerciseSessionRepository @Inject constructor(
    private val dao: ExerciseSessionDao,
) : ExerciseSessionRepository, PendingExerciseSessionSource {

    override suspend fun save(
        session: ExerciseSession,
    ) {
        dao.upsert(session.toEntity())
    }

    override suspend fun findById(
        sessionId: String,
    ): ExerciseSession? =
        dao.findById(sessionId)?.toDomain()
    override suspend fun findRecentCompletedByPatient(
        patientId: String,
        limit: Int,
    ): List<ExerciseSession> =
        dao.findRecentCompletedByPatient(
            patientId = patientId,
            limit = limit,
        ).map { entity ->
            entity.toDomain()
        }
    override suspend fun findPendingSessions(
        limit: Int,
    ): List<ExerciseSession> =
        dao.findPendingForSync(limit)
            .map { entity ->
                entity.toDomain()
            }
}
