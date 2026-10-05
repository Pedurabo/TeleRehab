package com.signaldesk.telerehab.domain.therapist

import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class CurrentWeekStartProvider @Inject constructor() {

    fun epochMillis(
        now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault()),
    ): Long =
        now
            .with(DayOfWeek.MONDAY)
            .toLocalDate()
            .atStartOfDay(now.zone)
            .toInstant()
            .toEpochMilli()
}
