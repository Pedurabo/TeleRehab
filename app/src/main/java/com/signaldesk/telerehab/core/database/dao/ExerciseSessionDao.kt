package com.signaldesk.telerehab.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.signaldesk.telerehab.core.database.entity.ExerciseSessionEntity

@Dao
interface ExerciseSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
