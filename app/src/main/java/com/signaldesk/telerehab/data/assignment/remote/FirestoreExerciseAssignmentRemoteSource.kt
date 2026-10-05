package com.signaldesk.telerehab.data.assignment.remote


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignmentRemoteSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreExerciseAssignmentRemoteSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val mapper: FirestoreExerciseAssignmentMapper,
) : ExerciseAssignmentRemoteSource {

    override suspend fun fetchForPatient(
        patientId: String,
    ): List<ExerciseAssignment> {
        val currentUserId =
            checkNotNull(
                firebaseAuth.currentUser?.uid,
            ) {
                "A signed-in Firebase user is required to refresh assignments."
            }

        require(currentUserId == patientId) {
            "The signed-in patient may only refresh their own assignments."
        }

        val snapshot =
            firestore
                .collection("patients")
                .document(patientId)
                .collection("exerciseAssignments")
                .get()
                .await()

        return snapshot.documents.map { document ->
            val assignment =
                mapper.toDomain(
                    documentId = document.id,
                    data = document.data.orEmpty(),
                )

            require(assignment.patientId == patientId) {
                "Remote assignment patientId does not match its patient path."
            }

            assignment
        }
    }
}
