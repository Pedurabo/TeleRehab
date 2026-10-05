package com.signaldesk.telerehab.data.therapist

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.signaldesk.telerehab.data.assignment.remote.FirestoreExerciseAssignmentMapper
import com.signaldesk.telerehab.domain.assignment.ExerciseAssignment
import com.signaldesk.telerehab.domain.therapist.TherapistExerciseAssignmentRemoteSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreTherapistExerciseAssignmentRemoteSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val mapper: FirestoreExerciseAssignmentMapper,
) : TherapistExerciseAssignmentRemoteSource {

    override suspend fun fetchForPatient(
        therapistId: String,
        patientId: String,
    ): List<ExerciseAssignment> {
        requireCurrentTherapist(therapistId)

        val snapshot =
            firestore
                .collection("patients")
                .document(patientId)
                .collection("exerciseAssignments")
                .get()
                .await()

        return snapshot.documents.map { document ->
            mapper.toDomain(
                documentId = document.id,
                data = document.data.orEmpty(),
            ).also { assignment ->
                require(assignment.patientId == patientId)
            }
        }
    }

    override suspend fun saveForPatient(
        therapistId: String,
        assignment: ExerciseAssignment,
    ) {
        requireCurrentTherapist(therapistId)

        firestore
            .collection("patients")
            .document(assignment.patientId)
            .collection("exerciseAssignments")
            .document(assignment.id)
            .set(
                mapOf(
                    "id" to assignment.id,
                    "patientId" to assignment.patientId,
                    "exerciseId" to assignment.exerciseId,
                    "title" to assignment.title,
                    "instructions" to assignment.instructions,
                    "targetRepetitions" to assignment.targetRepetitions,
                    "targetSessionsPerWeek" to assignment.targetSessionsPerWeek,
                    "flexedAtOrBelowDegrees" to assignment.flexedAtOrBelowDegrees,
                    "extendedAtOrAboveDegrees" to assignment.extendedAtOrAboveDegrees,
                    "status" to assignment.status.name,
                ),
            )
            .await()
    }

    private fun requireCurrentTherapist(
        therapistId: String,
    ) {
        val currentUserId =
            checkNotNull(firebaseAuth.currentUser?.uid) {
                "A signed-in Firebase user is required."
            }

        require(currentUserId == therapistId) {
            "The signed-in therapist may only act as themselves."
        }
    }
}
