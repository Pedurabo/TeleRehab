package com.signaldesk.telerehab.domain.auth

interface AuthSession {

    fun currentUserId(): String?

    suspend fun signInAnonymously(): String

    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String,
    ): String

    fun signOut()
}
