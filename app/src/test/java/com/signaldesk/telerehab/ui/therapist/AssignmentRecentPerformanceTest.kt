package com.signaldesk.telerehab.ui.therapist

import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentStatus
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionMetrics
import com.signaldesk.telerehab.domain.session.ExerciseSessionStatus
import com.signaldesk.telerehab.domain.session.SyncStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class AssignmentRecentPerformanceTest {

    @Test
    fun performanceIsCalculatedIndependentlyPerAssignment() {
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

        val sessions =
            listOf(
                session(
                    id = "flexion-latest",
                    assignmentId = kneeFlexion.id,
                    repetitions = 9,
                    minimumAngle = 80.0,
                    maximumAngle = 165.0,
                ),
                session(
                    id = "extension-latest",
                    assignmentId = kneeExtension.id,
                    repetitions = 5,
                    minimumAngle = 95.0,
                    maximumAngle = 165.0,
                ),
                session(
                    id = "flexion-previous",
                    assignmentId = kneeFlexion.id,
                    repetitions = 6,
                    minimumAngle = 100.0,
                    maximumAngle = 165.0,
                ),
                session(
                    id = "extension-previous",
                    assignmentId = kneeExtension.id,
                    repetitions = 7,
                    minimumAngle = 105.0,
                    maximumAngle = 165.0,
                ),
            )

        val performance =
            buildAssignmentRecentPerformance(
                assignments =
                    listOf(
                        kneeFlexion,
                        kneeExtension,
                    ),
                recentSessions = sessions,
            )

        assertEquals(2, performance.size)

        val flexion =
            performance[0]

        assertEquals(
            "knee-flexion-test",
            flexion.assignmentId,
        )
        assertEquals(
            "Knee Flexion",
            flexion.assignmentTitle,
        )
        assertEquals(
            2,
            flexion.completedSessionCount,
        )
        assertEquals(
            9,
            flexion.latestRepetitions,
        )
        assertEquals(
            3,
            flexion.repetitionTrend,
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
                flexion.kneeRangeTrendDegrees,
            ),
            0.0,
        )

        val extension =
            performance[1]

        assertEquals(
            "seated-knee-extension-test",
            extension.assignmentId,
        )
        assertEquals(
            "Seated Knee Extension",
            extension.assignmentTitle,
        )
        assertEquals(
            2,
            extension.completedSessionCount,
        )
        assertEquals(
            5,
            extension.latestRepetitions,
        )
        assertEquals(
            -2,
            extension.repetitionTrend,
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
                extension.kneeRangeTrendDegrees,
            ),
            0.0,
        )
    }

    @Test
    fun assignmentsWithoutSessionsAreNotIncluded() {
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

        val performance =
            buildAssignmentRecentPerformance(
                assignments =
                    listOf(
                        kneeFlexion,
                        kneeExtension,
                    ),
                recentSessions =
                    listOf(
                        session(
                            id = "flexion-only",
                            assignmentId =
                                kneeFlexion.id,
                            repetitions = 5,
                            minimumAngle = 90.0,
                            maximumAngle = 160.0,
                        ),
                    ),
            )

        assertEquals(1, performance.size)
        assertEquals(
            "knee-flexion-test",
            performance.single().assignmentId,
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
                    "2026-10-06T12:00:00Z",
                ),
            completedAt =
                Instant.parse(
                    "2026-10-06T12:10:00Z",
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
