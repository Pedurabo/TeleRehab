package com.signaldesk.telerehab.domain.identity

data class UserProfile(
    val id: String,
    val role: UserRole,
    val mustChangePassword: Boolean = false,
)
