package com.signaldesk.telerehab.core.time

import java.time.Instant
import javax.inject.Inject

class SystemAppClock @Inject constructor() : AppClock {
    override fun now(): Instant = Instant.now()
}
