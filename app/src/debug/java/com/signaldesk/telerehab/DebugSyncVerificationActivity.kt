package com.signaldesk.telerehab

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.signaldesk.telerehab.domain.auth.EnsureSignedIn
import com.signaldesk.telerehab.domain.session.ExerciseSessionRepository
import com.signaldesk.telerehab.domain.session.usecase.CompleteExerciseSession
import com.signaldesk.telerehab.domain.session.usecase.StartExerciseSession
import com.signaldesk.telerehab.domain.session.sync.SyncPendingSessions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class DebugSyncVerificationActivity : ComponentActivity() {

    @Inject
    lateinit var ensureSignedIn: EnsureSignedIn

    @Inject
    lateinit var startExerciseSession: StartExerciseSession

    @Inject
    lateinit var completeExerciseSession: CompleteExerciseSession

    @Inject
    lateinit var syncPendingSessions: SyncPendingSessions

    @Inject
    lateinit var repository: ExerciseSessionRepository

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                Log.i(TAG, "E2E_START")

                val uid =
                    ensureSignedIn()

                Log.i(TAG, "AUTH_GREEN|uid=$uid")

                val started =
                    startExerciseSession(
                        assignmentId = "debug-e2e-assignment",
                        patientId = uid,
                    )

                Log.i(
                    TAG,
                    "LOCAL_CREATED|id=${started.id}|sync=${started.syncStatus}",
                )

                check(started.patientId == uid)
                check(started.syncStatus.name == "PENDING")

                val completed =
                    completeExerciseSession(
                        sessionId = started.id,
                    )

                Log.i(
                    TAG,
                    "LOCAL_COMPLETED|id=${completed.id}|status=${completed.status}|sync=${completed.syncStatus}",
                )

                check(completed.status.name == "COMPLETED")
                check(completed.syncStatus.name == "PENDING")

                val syncResult =
                    syncPendingSessions()

                Log.i(
                    TAG,
                    "SYNC_RESULT|result=$syncResult",
                )

                check(syncResult.name == "COMPLETED")

                val localAfterSync =
                    repository.findById(completed.id)
                        ?: error("Local session disappeared after sync.")

                Log.i(
                    TAG,
                    "LOCAL_AFTER_SYNC|id=${localAfterSync.id}|sync=${localAfterSync.syncStatus}",
                )

                check(localAfterSync.syncStatus.name == "SYNCED")

                val remote =
                    firestore
                        .collection("patients")
                        .document(uid)
                        .collection("exerciseSessions")
                        .document(completed.id)
                        .get()
                        .await()

                check(remote.exists())

                check(remote.getString("id") == completed.id)
                check(remote.getString("patientId") == uid)
                check(
                    remote.getString("assignmentId") ==
                        "debug-e2e-assignment"
                )

                check(remote.getString("sessionStatus") == "COMPLETED")

                check(!remote.data.orEmpty().containsKey("syncStatus"))

                Log.i(
                    TAG,
                    "REMOTE_GREEN|path=patients/$uid/exerciseSessions/${completed.id}",
                )

                Log.i(
                    TAG,
                    "TELEREHAB_E2E_SYNC_GREEN|sessionId=${completed.id}",
                )
            } catch (error: Throwable) {
                Log.e(
                    TAG,
                    "TELEREHAB_E2E_SYNC_FAILED",
                    error,
                )
            } finally {
                finish()
            }
        }
    }

    private companion object {
        const val TAG =
            "TeleRehabE2E"
    }
}
