package com.signaldesk.telerehab.data.assignment.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface ExerciseAssignmentDao {

    @Upsert
    suspend fun upsert(
        assignment: ExerciseAssignmentEntity,
    )

    @Upsert
    suspend fun upsertAll(
        assignments: List<ExerciseAssignmentEntity>,
    )

    @Query(
        """
        SELECT *
        FROM exercise_assignments
        WHERE patientId = :patientId
          AND status = 'ACTIVE'
        ORDER BY title COLLATE NOCASE ASC
        """,
    )
    suspend fun findActiveByPatient(
        patientId: String,
    ): List<ExerciseAssignmentEntity>
}
