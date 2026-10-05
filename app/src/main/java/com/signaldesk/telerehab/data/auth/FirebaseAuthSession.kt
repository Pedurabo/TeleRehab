package com.signaldesk.telerehab.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.signaldesk.telerehab.domain.auth.AuthSession
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

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

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String,
    ): String {
        require(email.isNotBlank())
        require(password.isNotBlank())

        val result =
            firebaseAuth
                .signInWithEmailAndPassword(
                    email.trim(),
                    password,
                )
                .await()

        return requireNotNull(result.user?.uid) {
            "Firebase sign-in completed without a user ID."
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}
