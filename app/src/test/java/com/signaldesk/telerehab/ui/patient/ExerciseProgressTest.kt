package com.signaldesk.telerehab.ui.patient

import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentStatus
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionMetrics
import com.signaldesk.telerehab.domain.session.ExerciseSessionStatus
import com.signaldesk.telerehab.domain.session.SyncStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseProgressTest {

    @Test
    fun progressIsCalculatedIndependentlyForEachExercise() {
        val kneeFlexion =
            assignment(
                id = "knee-flexion-test",
                exerciseId = "knee-flexion",
                title = "Knee Flexion",
            )

        val kneeExtension =
            assignment(
                id = "seated-knee-extension-test",
                exerciseId = "seated-knee-extension",
                title = "Seated Knee Extension",
            )

        /*
         * Sessions are deliberately interleaved in newest-first order.
         * This catches accidental comparison across exercises.
         */
        val sessions =
            listOf(
                session(
                    id = "flexion-latest",
                    assignmentId = kneeFlexion.id,
                    repetitions = 8,
                    minimumAngle = 80.0,
                    maximumAngle = 165.0,
                ),
                session(
                    id = "extension-latest",
                    assignmentId = kneeExtension.id,
                    repetitions = 4,
                    minimumAngle = 95.0,
                    maximumAngle = 165.0,
                ),
                session(
                    id = "flexion-previous",
                    assignmentId = kneeFlexion.id,
                    repetitions = 5,
                    minimumAngle = 100.0,
                    maximumAngle = 165.0,
                ),
                session(
                    id = "extension-previous",
                    assignmentId = kneeExtension.id,
                    repetitions = 6,
                    minimumAngle = 105.0,
                    maximumAngle = 165.0,
                ),
            )

        val progress =
            buildExerciseProgressSummaries(
                assignments =
                    listOf(
                        kneeFlexion,
                        kneeExtension,
                    ),
                recentSessions = sessions,
            )

        assertEquals(2, progress.size)

        val flexion =
            progress[0]

        assertEquals(
            "knee-flexion-test",
            flexion.assignmentId,
        )
        assertEquals(
            "Knee Flexion",
            flexion.exerciseTitle,
        )
        assertEquals(
            2,
            flexion.completedSessionCount,
        )
        assertEquals(
            13,
            flexion.totalRepetitions,
        )
        assertEquals(
            8,
            flexion.latestRepetitions,
        )
        assertEquals(
            3,
            flexion.repetitionChangeFromPrevious,
        )
        assertEquals(
            85.0,
            requireNotNull(
                flexion.latestKneeRangeWidthDegrees,
            ),
            0.0,
        )
        assertEquals(
            20.0,
            requireNotNull(
                flexion.kneeRangeChangeFromPreviousDegrees,
            ),
            0.0,
        )

        val extension =
            progress[1]

        assertEquals(
            "seated-knee-extension-test",
            extension.assignmentId,
        )
        assertEquals(
            2,
            extension.completedSessionCount,
        )
        assertEquals(
            10,
            extension.totalRepetitions,
        )
        assertEquals(
            4,
            extension.latestRepetitions,
        )
        assertEquals(
            -2,
            extension.repetitionChangeFromPrevious,
        )
        assertEquals(
            70.0,
            requireNotNull(
                extension.latestKneeRangeWidthDegrees,
            ),
            0.0,
        )
        assertEquals(
            10.0,
            requireNotNull(
                extension.kneeRangeChangeFromPreviousDegrees,
            ),
            0.0,
        )
    }

    @Test
    fun sessionsWithoutAnActiveAssignmentAreNotShownAsProgress() {
        val assignment =
            assignment(
                id = "knee-flexion-test",
                exerciseId = "knee-flexion",
                title = "Knee Flexion",
            )

        val progress =
            buildExerciseProgressSummaries(
                assignments =
                    listOf(assignment),
                recentSessions =
                    listOf(
                        session(
                            id = "known",
                            assignmentId =
                                "knee-flexion-test",
                            repetitions = 5,
                            minimumAngle = 90.0,
                            maximumAngle = 160.0,
                        ),
                        session(
                            id = "old-assignment",
                            assignmentId =
                                "inactive-assignment",
                            repetitions = 10,
                            minimumAngle = 70.0,
                            maximumAngle = 170.0,
                        ),
                    ),
            )

        assertEquals(1, progress.size)
        assertEquals(
            "knee-flexion-test",
            progress.single().assignmentId,
        )
    }

    private fun assignment(
        id: String,
        exerciseId: String,
        title: String,
    ): ExerciseAssignment =
        ExerciseAssignment(
            id = id,
            patientId = "patient-1",
            exerciseId = exerciseId,
            title = title,
            instructions = "Test instructions",
            targetRepetitions = 10,
            targetSessionsPerWeek = 3,
            flexedAtOrBelowDegrees = 100.0,
            extendedAtOrAboveDegrees = 160.0,
            status = ExerciseAssignmentStatus.ACTIVE,
        )

    private fun session(
        id: String,
        assignmentId: String,
        repetitions: Int,
        minimumAngle: Double,
        maximumAngle: Double,
    ): ExerciseSession =
        ExerciseSession(
            id = id,
            assignmentId = assignmentId,
            patientId = "patient-1",
            startedAt =
                Instant.parse(
                    "2026-10-06T10:00:00Z",
                ),
            completedAt =
                Instant.parse(
                    "2026-10-06T10:10:00Z",
                ),
            status =
                ExerciseSessionStatus.COMPLETED,
            syncStatus =
                SyncStatus.SYNCED,
            metrics =
                ExerciseSessionMetrics(
                    completedRepetitions =
                        repetitions,
                    minimumKneeAngleDegrees =
                        minimumAngle,
                    maximumKneeAngleDegrees =
                        maximumAngle,
                ),
        )
}
