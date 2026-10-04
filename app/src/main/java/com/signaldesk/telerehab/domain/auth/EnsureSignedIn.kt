package com.signaldesk.telerehab.domain.auth

import javax.inject.Inject

class EnsureSignedIn @Inject constructor(
    private val authSession: AuthSession,
) {

    suspend operator fun invoke(): String =
        authSession.currentUserId()
            ?: authSession.signInAnonymously()
}
