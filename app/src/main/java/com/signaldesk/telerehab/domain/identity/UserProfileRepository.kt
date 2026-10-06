package com.signaldesk.telerehab.domain.identity

interface UserProfileRepository {

    suspend fun findById(
        userId: String,
    ): UserProfile?

    suspend fun markPasswordChanged(
        userId: String,
    ) {
        error("Password-change profile updates are not supported.")
    }
}
