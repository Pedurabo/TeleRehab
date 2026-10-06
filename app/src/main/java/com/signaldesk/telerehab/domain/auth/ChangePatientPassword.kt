package com.signaldesk.telerehab.domain.auth

import com.signaldesk.telerehab.domain.identity.UserProfileRepository
import javax.inject.Inject

class ChangePatientPassword @Inject constructor(
    private val authSession: AuthSession,
    private val userProfileRepository: UserProfileRepository,
) {

    suspend operator fun invoke(
        newPassword: String,
    ) {
        require(newPassword.length >= 8) {
            "Password must contain at least 8 characters."
        }

        val userId =
            requireNotNull(authSession.currentUserId()) {
                "No patient is currently signed in."
            }

        authSession.updatePassword(
            newPassword = newPassword,
        )

        userProfileRepository.markPasswordChanged(
            userId = userId,
        )
    }
}
