package com.signaldesk.telerehab.domain.auth

interface AuthSession {

    fun currentUserId(): String?

    suspend fun signInAnonymously(): String

    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String,
    ): String

    suspend fun updatePassword(
        newPassword: String,
    ) {
        error("Password updates are not supported.")
    }

    fun signOut()
}
