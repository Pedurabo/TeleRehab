package com.signaldesk.telerehab.domain.therapist

import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyAdherenceCalculatorTest {

    private val calculator =
        WeeklyAdherenceCalculator()

    @Test
    fun zeroCompletedSessionsReturnsNotStarted() {
        val result =
            calculator.calculate(
                completedSessions = 0,
                targetSessions = 3,
            )

        assertEquals(0, result.percent)
        assertEquals(
            WeeklyAdherenceStatus.NOT_STARTED,
            result.status,
        )
    }

    @Test
    fun partialAdherenceReturnsInProgress() {
        val result =
            calculator.calculate(
                completedSessions = 2,
                targetSessions = 3,
            )

        assertEquals(66, result.percent)
        assertEquals(
            WeeklyAdherenceStatus.IN_PROGRESS,
            result.status,
        )
    }

    @Test
    fun meetingTargetReturnsComplete() {
        val result =
            calculator.calculate(
                completedSessions = 3,
                targetSessions = 3,
            )

        assertEquals(100, result.percent)
        assertEquals(
            WeeklyAdherenceStatus.COMPLETE,
            result.status,
        )
    }

    @Test
    fun exceedingTargetStaysCompleteAndCapsPercent() {
        val result =
            calculator.calculate(
                completedSessions = 5,
                targetSessions = 3,
            )

        assertEquals(100, result.percent)
        assertEquals(
            WeeklyAdherenceStatus.COMPLETE,
            result.status,
        )
    }
}
