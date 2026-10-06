package com.signaldesk.telerehab.data.identity

import com.google.firebase.firestore.FirebaseFirestore
import com.signaldesk.telerehab.domain.identity.UserProfile
import com.signaldesk.telerehab.domain.identity.UserProfileRepository
import com.signaldesk.telerehab.domain.identity.UserRole
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreUserProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserProfileRepository {

    override suspend fun findById(
        userId: String,
    ): UserProfile? {
        val document =
            firestore
                .collection("users")
                .document(userId)
                .get()
                .await()

        if (!document.exists()) {
            return null
        }

        val role =
            document.getString("role")
                ?: return null

        return UserProfile(
            id = userId,
            role = UserRole.valueOf(role),
            mustChangePassword =
                document.getBoolean("mustChangePassword")
                    ?: false,
        )
    }

    override suspend fun markPasswordChanged(
        userId: String,
    ) {
        firestore
            .collection("users")
            .document(userId)
            .update(
                mapOf(
                    "mustChangePassword" to false,
                ),
            )
            .await()
    }
}
