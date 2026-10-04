package com.signaldesk.telerehab.domain.auth

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EnsureSignedInTest {

    @Test
    fun `returns existing user without creating another session`() =
        runTest {
            val authSession =
                RecordingAuthSession(
                    existingUserId = "existing-user",
                    anonymousUserId = "anonymous-user",
                )

            val userId =
                EnsureSignedIn(authSession)()

            assertEquals("existing-user", userId)
            assertEquals(0, authSession.signInCalls)
        }

    @Test
    fun `creates anonymous session when signed out`() =
        runTest {
            val authSession =
                RecordingAuthSession(
                    existingUserId = null,
                    anonymousUserId = "anonymous-user",
                )

            val userId =
                EnsureSignedIn(authSession)()

            assertEquals("anonymous-user", userId)
            assertEquals(1, authSession.signInCalls)
        }

    private class RecordingAuthSession(
        private val existingUserId: String?,
        private val anonymousUserId: String,
    ) : AuthSession {

        var signInCalls: Int = 0
            private set

        override fun currentUserId(): String? =
            existingUserId

        override suspend fun signInAnonymously(): String {
            signInCalls += 1
            return anonymousUserId
        }
    }
}
