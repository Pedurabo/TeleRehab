package com.signaldesk.telerehab.domain.therapist

import com.signaldesk.telerehab.domain.session.ExerciseSession
import javax.inject.Inject

class GetPatientRecentSessionsForTherapist @Inject constructor(
    private val remoteSource: TherapistExerciseSessionRemoteSource,
) {
    suspend operator fun invoke(
        therapistId: String,
        patientId: String,
        limit: Int = 5,
    ): List<ExerciseSession> {
        require(therapistId.isNotBlank())
        require(patientId.isNotBlank())
        require(limit > 0)

        return remoteSource.fetchRecentCompletedForPatient(
            therapistId = therapistId,
            patientId = patientId,
            limit = limit,
        )
    }
}
