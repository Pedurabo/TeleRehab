package com.signaldesk.telerehab.domain.session

interface ExerciseSessionRepository {

    suspend fun save(
        session: ExerciseSession,
    )

    suspend fun findById(
        sessionId: String,
    ): ExerciseSession?
    suspend fun findRecentCompletedByPatient(
        patientId: String,
        limit: Int,
    ): List<ExerciseSession>
}
