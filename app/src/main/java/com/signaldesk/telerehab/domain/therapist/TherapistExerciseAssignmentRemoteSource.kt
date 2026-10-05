package com.signaldesk.telerehab.domain.therapist

import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment

interface TherapistExerciseAssignmentRemoteSource {

    suspend fun fetchForPatient(
        therapistId: String,
        patientId: String,
    ): List<ExerciseAssignment>

    suspend fun saveForPatient(
        therapistId: String,
        assignment: ExerciseAssignment,
    )
}
