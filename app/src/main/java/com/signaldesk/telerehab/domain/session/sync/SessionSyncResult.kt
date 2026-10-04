package com.signaldesk.telerehab.domain.session.sync

enum class SessionSyncResult {
    SUCCESS,
    RETRYABLE_FAILURE,
    PERMANENT_FAILURE,
}

enum class SyncBatchResult {
    COMPLETED,
    RETRY,
}
