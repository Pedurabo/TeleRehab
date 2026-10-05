package com.signaldesk.telerehab.domain.therapist

import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import javax.inject.Inject

class GetPatientAssignmentsForTherapist @Inject constructor(
    private val remoteSource: TherapistExerciseAssignmentRemoteSource,
) {

    suspend operator fun invoke(
        therapistId: String,
        patientId: String,
    ): List<ExerciseAssignment> {
        require(therapistId.isNotBlank())
        require(patientId.isNotBlank())

        return remoteSource.fetchForPatient(
            therapistId = therapistId,
            patientId = patientId,
        )
    }
}
