package com.signaldesk.telerehab.domain.therapist

import javax.inject.Inject

class WeeklyAdherenceCalculator @Inject constructor() {

    fun calculate(
        completedSessions: Int,
        targetSessions: Int,
    ): Int {
        require(completedSessions >= 0)
        require(targetSessions > 0)

        return ((completedSessions * 100) / targetSessions)
            .coerceAtMost(100)
    }
}
