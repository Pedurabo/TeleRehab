package com.signaldesk.telerehab.domain.therapist

import javax.inject.Inject

data class WeeklyAdherenceResult(
    val percent: Int,
    val status: WeeklyAdherenceStatus,
)

class WeeklyAdherenceCalculator @Inject constructor() {

    fun calculate(
        completedSessions: Int,
        targetSessions: Int,
    ): WeeklyAdherenceResult {
        require(completedSessions >= 0)
        require(targetSessions > 0)

        val percent =
            ((completedSessions * 100) / targetSessions)
                .coerceAtMost(100)

        val status =
            when {
                completedSessions == 0 ->
                    WeeklyAdherenceStatus.NOT_STARTED

                completedSessions >= targetSessions ->
                    WeeklyAdherenceStatus.COMPLETE

                else ->
                    WeeklyAdherenceStatus.IN_PROGRESS
            }

        return WeeklyAdherenceResult(
            percent = percent,
            status = status,
        )
    }
}
