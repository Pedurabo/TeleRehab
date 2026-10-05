package com.signaldesk.telerehab.domain.therapist

import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import javax.inject.Inject

class SavePatientAssignmentForTherapist @Inject constructor(
    private val remoteSource: TherapistExerciseAssignmentRemoteSource,
) {

    suspend operator fun invoke(
        therapistId: String,
        assignment: ExerciseAssignment,
    ) {
        require(therapistId.isNotBlank())
        require(assignment.patientId.isNotBlank())

        remoteSource.saveForPatient(
            therapistId = therapistId,
            assignment = assignment,
        )
    }
}
