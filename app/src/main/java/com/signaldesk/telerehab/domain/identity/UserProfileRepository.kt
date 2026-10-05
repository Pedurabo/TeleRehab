package com.signaldesk.telerehab.domain.identity

interface UserProfileRepository {

    suspend fun findById(
        userId: String,
    ): UserProfile?
}
