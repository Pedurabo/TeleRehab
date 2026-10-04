package com.signaldesk.telerehab.domain.assignment

interface ExerciseAssignmentRepository {

    suspend fun findActiveByPatient(
        patientId: String,
    ): List<ExerciseAssignment>
}
