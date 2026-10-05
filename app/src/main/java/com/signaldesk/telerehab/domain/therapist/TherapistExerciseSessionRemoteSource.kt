package com.signaldesk.telerehab.domain.therapist

import com.signaldesk.telerehab.domain.session.ExerciseSession

interface TherapistExerciseSessionRemoteSource {
    suspend fun fetchRecentCompletedForPatient(
        therapistId: String,
        patientId: String,
        limit: Int,
    ): List<ExerciseSession>
}
