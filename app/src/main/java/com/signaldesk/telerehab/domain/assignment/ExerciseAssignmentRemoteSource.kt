package com.signaldesk.telerehab.domain.assignment

interface ExerciseAssignmentRemoteSource {

    suspend fun fetchForPatient(
        patientId: String,
    ): List<ExerciseAssignment>
}
