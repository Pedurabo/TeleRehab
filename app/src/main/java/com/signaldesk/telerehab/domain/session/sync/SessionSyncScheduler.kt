package com.signaldesk.telerehab.domain.session.sync

fun interface SessionSyncScheduler {

    fun requestSync()

    companion object {
        val NoOp =
            SessionSyncScheduler {
                // Used only when a caller intentionally omits scheduling,
                // such as focused domain unit tests.
            }
    }
}
