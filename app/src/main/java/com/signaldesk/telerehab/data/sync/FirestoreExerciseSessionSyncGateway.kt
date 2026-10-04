package com.signaldesk.telerehab.data.sync

import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.sync.ExerciseSessionSyncGateway
import com.signaldesk.telerehab.domain.session.sync.SessionSyncResult
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreExerciseSessionSyncGateway @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ExerciseSessionSyncGateway {

    override suspend fun upsertSession(
        session: ExerciseSession,
    ): SessionSyncResult =
        try {
            firestore
                .collection(SESSIONS_COLLECTION)
                .document(session.id)
                .set(session.toRemoteDocument())
                .await()

            SessionSyncResult.SUCCESS
        } catch (exception: FirebaseFirestoreException) {
            exception.toSyncResult()
        } catch (_: FirebaseException) {
            SessionSyncResult.RETRYABLE_FAILURE
        }

    private fun ExerciseSession.toRemoteDocument(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "assignmentId" to assignmentId,
            "patientId" to patientId,
            "startedAtEpochMillis" to startedAt.toEpochMilli(),
            "completedAtEpochMillis" to completedAt?.toEpochMilli(),
            "sessionStatus" to status.name,
            "syncStatus" to syncStatus.name,
        )

    private fun FirebaseFirestoreException.toSyncResult(): SessionSyncResult =
        when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            FirebaseFirestoreException.Code.UNAUTHENTICATED,
            FirebaseFirestoreException.Code.INVALID_ARGUMENT,
            FirebaseFirestoreException.Code.FAILED_PRECONDITION,
            -> SessionSyncResult.PERMANENT_FAILURE

            else -> SessionSyncResult.RETRYABLE_FAILURE
        }

    companion object {
        private const val SESSIONS_COLLECTION = "exerciseSessions"
    }
}
