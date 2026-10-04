package com.signaldesk.telerehab.data.assignment

import com.signaldesk.telerehab.data.assignment.local.ExerciseAssignmentEntity
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseAssignmentMapperTest {

    private val mapper =
        ExerciseAssignmentMapper()

    @Test
    fun mapsEntityToDomain() {
        val entity =
            ExerciseAssignmentEntity(
                id = "assignment-1",
                patientId = "patient-1",
                exerciseId = "knee-flexion-extension",
                title = "Knee Flexion and Extension",
                instructions = "Bend and straighten the knee.",
                targetRepetitions = 10,
                status = "ACTIVE",
            )

        val result =
            mapper.toDomain(entity)

        assertEquals(
            ExerciseAssignment(
                id = "assignment-1",
                patientId = "patient-1",
                exerciseId = "knee-flexion-extension",
                title = "Knee Flexion and Extension",
                instructions = "Bend and straighten the knee.",
                targetRepetitions = 10,
                status = ExerciseAssignmentStatus.ACTIVE,
            ),
            result,
        )
    }

    @Test
    fun mapsDomainToEntity() {
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

        val result =
            mapper.toEntity(assignment)

        assertEquals("assignment-1", result.id)
        assertEquals("patient-1", result.patientId)
        assertEquals("knee-flexion-extension", result.exerciseId)
        assertEquals("ACTIVE", result.status)
        assertEquals(10, result.targetRepetitions)
    }
}
