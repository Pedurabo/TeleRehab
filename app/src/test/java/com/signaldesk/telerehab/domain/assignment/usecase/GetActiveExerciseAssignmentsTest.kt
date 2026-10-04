package com.signaldesk.telerehab.domain.assignment.usecase

import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentRepository
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetActiveExerciseAssignmentsTest {

    @Test
    fun returnsAssignmentsForPatient() = runTest {
        val expected =
            listOf(
                ExerciseAssignment(
                    id = "assignment-1",
                    patientId = "patient-1",
                    exerciseId = "knee-flexion-extension",
                    title = "Knee Flexion and Extension",
                    instructions = "Bend and straighten the knee through the prescribed range.",
                    targetRepetitions = 10,
                    status = ExerciseAssignmentStatus.ACTIVE,
                ),
            )

        val repository =
            RecordingRepository(
                assignments = expected,
            )

        val useCase =
            GetActiveExerciseAssignments(
                repository = repository,
            )

        val result =
            useCase(
                patientId = "patient-1",
            )

        assertEquals(
            "patient-1",
            repository.requestedPatientId,
        )

        assertEquals(
            expected,
            result,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsBlankPatientId() = runTest {
        val useCase =
            GetActiveExerciseAssignments(
                repository =
                    RecordingRepository(
                        assignments = emptyList(),
                    ),
            )

        useCase(
            patientId = " ",
        )
    }

    private class RecordingRepository(
        private val assignments: List<ExerciseAssignment>,
    ) : ExerciseAssignmentRepository {

        var requestedPatientId: String? = null

        override suspend fun findActiveByPatient(
            patientId: String,
        ): List<ExerciseAssignment> {
            requestedPatientId =
                patientId

            return assignments
        }
    }
}
