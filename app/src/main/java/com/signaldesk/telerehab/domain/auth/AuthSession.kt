package com.signaldesk.telerehab.domain.auth

interface AuthSession {

    fun currentUserId(): String?

    suspend fun signInAnonymously(): String
}
