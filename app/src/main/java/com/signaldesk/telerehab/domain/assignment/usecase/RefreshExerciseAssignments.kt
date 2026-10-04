package com.signaldesk.telerehab.domain.assignment.usecase

import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentRemoteSource
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentRepository
import javax.inject.Inject

class RefreshExerciseAssignments @Inject constructor(
    private val remoteSource: ExerciseAssignmentRemoteSource,
    private val repository: ExerciseAssignmentRepository,
) {

    suspend operator fun invoke(
        patientId: String,
    ) {
        require(patientId.isNotBlank()) {
            "Patient ID must not be blank."
        }

        val assignments =
            remoteSource.fetchForPatient(
                patientId = patientId,
            )

        require(
            assignments.all {
                it.patientId == patientId
            },
        ) {
            "Remote assignments must belong to the requested patient."
        }

        repository.replaceForPatient(
            patientId = patientId,
            assignments = assignments,
        )
    }
}
