package com.signaldesk.telerehab.domain.therapist

import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyAdherenceCalculatorTest {

    private val calculator =
        WeeklyAdherenceCalculator()

    @Test
    fun zeroCompletedSessionsReturnsZeroPercent() {
        assertEquals(
            0,
            calculator.calculate(
                completedSessions = 0,
                targetSessions = 3,
            ),
        )
    }

    @Test
    fun partialAdherenceUsesWholePercentage() {
        assertEquals(
            66,
            calculator.calculate(
                completedSessions = 2,
                targetSessions = 3,
            ),
        )
    }

    @Test
    fun meetingTargetReturnsOneHundredPercent() {
        assertEquals(
            100,
            calculator.calculate(
                completedSessions = 3,
                targetSessions = 3,
            ),
        )
    }

    @Test
    fun exceedingTargetIsCappedAtOneHundredPercent() {
        assertEquals(
            100,
            calculator.calculate(
                completedSessions = 5,
                targetSessions = 3,
            ),
        )
    }
}
