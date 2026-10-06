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

    override suspend fun updatePassword(
        newPassword: String,
    ) {
        require(newPassword.length >= 8) {
            "Password must contain at least 8 characters."
        }

        val user =
            requireNotNull(firebaseAuth.currentUser) {
                "No patient is currently signed in."
            }

        user
            .updatePassword(newPassword)
            .await()
    }

    override suspend fun sendPasswordResetEmail(
        email: String,
    ) {
        require(email.isNotBlank()) {
            "Email is required."
        }

        firebaseAuth
            .sendPasswordResetEmail(
                email.trim(),
            )
            .await()
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}
