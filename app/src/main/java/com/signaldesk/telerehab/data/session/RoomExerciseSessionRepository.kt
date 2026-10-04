package com.signaldesk.telerehab.data.session

import com.signaldesk.telerehab.core.database.dao.ExerciseSessionDao
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionRepository
import javax.inject.Inject

class RoomExerciseSessionRepository @Inject constructor(
    private val dao: ExerciseSessionDao,
) : ExerciseSessionRepository {

    override suspend fun save(
        session: ExerciseSession,
    ) {
        dao.upsert(session.toEntity())
    }

    override suspend fun findById(
        sessionId: String,
    ): ExerciseSession? =
        dao.findById(sessionId)?.toDomain()
}
