package com.signaldesk.telerehab.data.assignment.remote

import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class FirestoreExerciseAssignmentMapperTest {

    private val mapper =
        FirestoreExerciseAssignmentMapper()

    @Test
    fun mapsValidFirestoreDocument() {
        val result =
            mapper.toDomain(
                documentId = "assignment-1",
                data =
                    mapOf(
                        "id" to "assignment-1",
                        "patientId" to "patient-1",
                        "exerciseId" to "knee-flexion-extension",
                        "title" to "Knee Flexion and Extension",
                        "instructions" to "Bend and straighten the knee.",
                        "targetRepetitions" to 10L,
                        "status" to "ACTIVE",
                    ),
            )

        assertEquals(
            "assignment-1",
            result.id,
        )

        assertEquals(
            "patient-1",
            result.patientId,
        )

        assertEquals(
            10,
            result.targetRepetitions,
        )

        assertEquals(
            ExerciseAssignmentStatus.ACTIVE,
            result.status,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDocumentIdMismatch() {
        mapper.toDomain(
            documentId = "document-1",
            data =
                mapOf(
                    "id" to "different-id",
                    "patientId" to "patient-1",
                    "exerciseId" to "knee-flexion-extension",
                    "title" to "Knee Flexion and Extension",
                    "instructions" to "Bend and straighten the knee.",
                    "targetRepetitions" to 10L,
                    "status" to "ACTIVE",
                ),
        )
    }
}
