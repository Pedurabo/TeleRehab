package com.signaldesk.telerehab.domain.session

import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseSessionMetricsTest {

    @Test
    fun acceptsDerivedMetrics() {
        val metrics =
            ExerciseSessionMetrics(
                completedRepetitions = 8,
                minimumKneeAngleDegrees = 82.5,
                maximumKneeAngleDegrees = 171.0,
            )

        assertEquals(
            8,
            metrics.completedRepetitions,
        )
    }

    @Test(
        expected = IllegalArgumentException::class,
    )
    fun rejectsNegativeRepetitions() {
        ExerciseSessionMetrics(
            completedRepetitions = -1,
        )
    }

    @Test(
        expected = IllegalArgumentException::class,
    )
    fun rejectsPartialAngleSummary() {
        ExerciseSessionMetrics(
            completedRepetitions = 3,
            minimumKneeAngleDegrees = 90.0,
        )
    }

    @Test(
        expected = IllegalArgumentException::class,
    )
    fun rejectsInvertedAngleSummary() {
        ExerciseSessionMetrics(
            completedRepetitions = 3,
            minimumKneeAngleDegrees = 170.0,
            maximumKneeAngleDegrees = 80.0,
        )
    }
}
