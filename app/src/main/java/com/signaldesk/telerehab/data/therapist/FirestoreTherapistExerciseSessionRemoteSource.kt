package com.signaldesk.telerehab.data.therapist

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionMetrics
import com.signaldesk.telerehab.domain.session.ExerciseSessionStatus
import com.signaldesk.telerehab.domain.session.SyncStatus
import com.signaldesk.telerehab.domain.therapist.TherapistExerciseSessionRemoteSource
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreTherapistExerciseSessionRemoteSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : TherapistExerciseSessionRemoteSource {

    override suspend fun fetchRecentCompletedForPatient(
        therapistId: String,
        patientId: String,
        limit: Int,
    ): List<ExerciseSession> {
        requireCurrentTherapist(therapistId)
        require(patientId.isNotBlank())
        require(limit > 0)

        return fetchCompletedSessions(patientId)
            .sortedByDescending { it.completedAt }
            .take(limit)
    }

    override suspend fun fetchCompletedForPatientSince(
        therapistId: String,
        patientId: String,
        sinceEpochMillis: Long,
    ): List<ExerciseSession> {
        requireCurrentTherapist(therapistId)
        require(patientId.isNotBlank())
        require(sinceEpochMillis >= 0L)

        return fetchCompletedSessions(patientId)
            .filter { session ->
                val completedAt =
                    session.completedAt
                        ?: return@filter false

                completedAt.toEpochMilli() >= sinceEpochMillis
            }
            .sortedByDescending { it.completedAt }
    }

    private suspend fun fetchCompletedSessions(
        patientId: String,
    ): List<ExerciseSession> {
        val snapshot =
            firestore
                .collection("patients")
                .document(patientId)
                .collection("exerciseSessions")
                .get()
                .await()

        return snapshot.documents
            .mapNotNull { document ->
                document.toCompletedSession()
            }
            .filter { session ->
                session.patientId == patientId
            }
    }

    private fun DocumentSnapshot.toCompletedSession(): ExerciseSession? {
        val data = data.orEmpty()

        if (data["sessionStatus"] != ExerciseSessionStatus.COMPLETED.name) {
            return null
        }

        val completedAtEpochMillis =
            (data["completedAtEpochMillis"] as? Number)?.toLong()
                ?: return null

        val completedRepetitions =
            (data["completedRepetitions"] as? Number)?.toInt()

        val minimumAngle =
            (data["minimumKneeAngleDegrees"] as? Number)?.toDouble()

        val maximumAngle =
            (data["maximumKneeAngleDegrees"] as? Number)?.toDouble()

        val metrics =
            if (completedRepetitions != null) {
                ExerciseSessionMetrics(
                    completedRepetitions = completedRepetitions,
                    minimumKneeAngleDegrees = minimumAngle,
                    maximumKneeAngleDegrees = maximumAngle,
                )
            } else {
                null
            }

        return ExerciseSession(
            id = id,
            assignmentId =
                requireNotNull(data["assignmentId"] as? String),
            patientId =
                requireNotNull(data["patientId"] as? String),
            startedAt =
                Instant.ofEpochMilli(
                    requireNotNull(
                        (data["startedAtEpochMillis"] as? Number)
                            ?.toLong(),
                    ),
                ),
            completedAt =
                Instant.ofEpochMilli(completedAtEpochMillis),
            status = ExerciseSessionStatus.COMPLETED,
            syncStatus = SyncStatus.SYNCED,
            metrics = metrics,
        )
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
