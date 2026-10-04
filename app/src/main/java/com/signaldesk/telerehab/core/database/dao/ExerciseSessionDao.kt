package com.signaldesk.telerehab.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.signaldesk.telerehab.core.database.entity.ExerciseSessionEntity

@Dao
interface ExerciseSessionDao {

    @Upsert
    suspend fun upsert(session: ExerciseSessionEntity)

    @Query(
        """
        SELECT *
        FROM exercise_sessions
        WHERE id = :sessionId
        LIMIT 1
        """,
    )
    suspend fun findById(
        sessionId: String,
    ): ExerciseSessionEntity?
}
