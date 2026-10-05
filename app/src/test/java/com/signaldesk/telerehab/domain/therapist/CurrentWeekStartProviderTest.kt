package com.signaldesk.telerehab.domain.therapist

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrentWeekStartProviderTest {

    private val provider =
        CurrentWeekStartProvider()

    private val zone =
        ZoneId.of("Africa/Kampala")

    @Test
    fun mondayReturnsStartOfSameDay() {
        val now =
            ZonedDateTime.of(
                2026,
                10,
                5,
                14,
                0,
                0,
                0,
                zone,
            )

        val expected =
            ZonedDateTime.of(
                2026,
                10,
                5,
                0,
                0,
                0,
                0,
                zone,
            )
                .toInstant()
                .toEpochMilli()

        assertEquals(
            expected,
            provider.epochMillis(now),
        )
    }

    @Test
    fun midweekReturnsPreviousMondayStart() {
        val now =
            ZonedDateTime.of(
                2026,
                10,
                8,
                11,
                30,
                0,
                0,
                zone,
            )

        val expected =
            ZonedDateTime.of(
                2026,
                10,
                5,
                0,
                0,
                0,
                0,
                zone,
            )
                .toInstant()
                .toEpochMilli()

        assertEquals(
            expected,
            provider.epochMillis(now),
        )
    }
}
