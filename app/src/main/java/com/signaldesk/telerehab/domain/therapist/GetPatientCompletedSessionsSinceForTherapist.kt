package com.signaldesk.telerehab.domain.therapist

import com.signaldesk.telerehab.domain.session.ExerciseSession
import javax.inject.Inject

class GetPatientCompletedSessionsSinceForTherapist @Inject constructor(
    private val remoteSource: TherapistExerciseSessionRemoteSource,
) {
    suspend operator fun invoke(
        therapistId: String,
        patientId: String,
        sinceEpochMillis: Long,
    ): List<ExerciseSession> {
        require(therapistId.isNotBlank())
        require(patientId.isNotBlank())
        require(sinceEpochMillis >= 0L)

        return remoteSource.fetchCompletedForPatientSince(
            therapistId = therapistId,
            patientId = patientId,
            sinceEpochMillis = sinceEpochMillis,
        )
    }
}
