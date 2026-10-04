package com.signaldesk.telerehab.core.time

import java.time.Instant

interface AppClock {
    fun now(): Instant
}
