package com.signaldesk.telerehab.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.signaldesk.telerehab.domain.auth.AuthSession
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthSession @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : AuthSession {

    override fun currentUserId(): String? =
        firebaseAuth.currentUser?.uid

    override suspend fun signInAnonymously(): String {
        val result =
            firebaseAuth
                .signInAnonymously()
                .await()

        return requireNotNull(result.user?.uid) {
            "Firebase anonymous sign-in completed without a user ID."
        }
    }
}
