package com.signaldesk.telerehab.domain.auth

import com.signaldesk.telerehab.domain.identity.UserProfileRepository
import com.signaldesk.telerehab.domain.identity.UserRole
import javax.inject.Inject

class SignInPatient @Inject constructor(
    private val authSession: AuthSession,
    private val userProfileRepository: UserProfileRepository,
) {

    suspend operator fun invoke(
        email: String,
        password: String,
    ): String {
        val userId =
            authSession.signInWithEmailAndPassword(
                email = email,
                password = password,
            )

        val profile =
            userProfileRepository.findById(
                userId = userId,
            )

        check(profile?.role == UserRole.PATIENT) {
            authSession.signOut()
            "The signed-in account is not authorized as a patient."
        }

        return userId
    }
}
