package com.signaldesk.telerehab.domain.assignment.usecase

import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentRemoteSource
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentRepository
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshExerciseAssignmentsTest {

    @Test
    fun replacesLocalAssignmentsWithRemoteSnapshot() = runTest {
        val assignment =
            ExerciseAssignment(
                id = "assignment-1",
                patientId = "patient-1",
                exerciseId = "knee-flexion-extension",
                title = "Knee Flexion and Extension",
                instructions = "Bend and straighten the knee.",
                targetRepetitions = 10,
                status = ExerciseAssignmentStatus.ACTIVE,
            )

        val remote =
            FakeRemoteSource(
                assignments = listOf(assignment),
            )

        val repository =
            RecordingRepository()

        val useCase =
            RefreshExerciseAssignments(
                remoteSource = remote,
                repository = repository,
            )

        useCase(
            patientId = "patient-1",
        )

        assertEquals(
            "patient-1",
            remote.requestedPatientId,
        )

        assertEquals(
            "patient-1",
            repository.replacedPatientId,
        )

        assertEquals(
            listOf(assignment),
            repository.replacedAssignments,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAssignmentsForAnotherPatient() = runTest {
        val remote =
            FakeRemoteSource(
                assignments =
                    listOf(
                        ExerciseAssignment(
                            id = "assignment-1",
                            patientId = "patient-2",
                            exerciseId = "knee-flexion-extension",
                            title = "Knee Flexion and Extension",
                            instructions = "Bend and straighten the knee.",
                            targetRepetitions = 10,
                            status = ExerciseAssignmentStatus.ACTIVE,
                        ),
                    ),
            )

        val useCase =
            RefreshExerciseAssignments(
                remoteSource = remote,
                repository = RecordingRepository(),
            )

        useCase(
            patientId = "patient-1",
        )
    }

    private class FakeRemoteSource(
        private val assignments: List<ExerciseAssignment>,
    ) : ExerciseAssignmentRemoteSource {

        var requestedPatientId: String? = null

        override suspend fun fetchForPatient(
            patientId: String,
        ): List<ExerciseAssignment> {
            requestedPatientId =
                patientId

            return assignments
        }
    }

    private class RecordingRepository :
        ExerciseAssignmentRepository {

        var replacedPatientId: String? = null

        var replacedAssignments:
            List<ExerciseAssignment> = emptyList()

        override suspend fun findActiveByPatient(
            patientId: String,
        ): List<ExerciseAssignment> =
            emptyList()

        override suspend fun replaceForPatient(
            patientId: String,
            assignments: List<ExerciseAssignment>,
        ) {
            replacedPatientId =
                patientId

            replacedAssignments =
                assignments
        }
    }
}
