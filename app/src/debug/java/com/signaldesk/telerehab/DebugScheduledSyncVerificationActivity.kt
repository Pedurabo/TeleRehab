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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class DebugScheduledSyncVerificationActivity : ComponentActivity() {

    @Inject
    lateinit var ensureSignedIn: EnsureSignedIn

    @Inject
    lateinit var startExerciseSession: StartExerciseSession

    @Inject
    lateinit var completeExerciseSession: CompleteExerciseSession

    @Inject
    lateinit var repository: ExerciseSessionRepository

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                Log.i(TAG, "SCHEDULED_E2E_START")

                val uid =
                    ensureSignedIn()

                Log.i(TAG, "AUTH_GREEN|uid=$uid")

                val started =
                    startExerciseSession(
                        assignmentId = "debug-workmanager-assignment",
                        patientId = uid,
                    )

                Log.i(
                    TAG,
                    "LOCAL_CREATED|id=${started.id}|sync=${started.syncStatus}",
                )

                val completed =
                    completeExerciseSession(
                        sessionId = started.id,
                    )

                Log.i(
                    TAG,
                    "LOCAL_COMPLETED|id=${completed.id}|sync=${completed.syncStatus}",
                )

                check(completed.syncStatus.name == "PENDING")

                /*
                 * Important:
                 * There is deliberately NO call to SyncPendingSessions here.
                 *
                 * CompleteExerciseSession must request WorkManager work.
                 */

                var synced = false

                repeat(40) {
                    delay(500)

                    val current =
                        repository.findById(completed.id)
                            ?: error("Session disappeared.")

                    if (current.syncStatus.name == "SYNCED") {
                        synced = true

                        Log.i(
                            TAG,
                            "WORKMANAGER_LOCAL_SYNCED|id=${current.id}",
                        )

                        return@repeat
                    }
                }

                check(synced) {
                    "Session never reached SYNCED through WorkManager."
                }

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
                check(remote.getString("sessionStatus") == "COMPLETED")
                check(!remote.data.orEmpty().containsKey("syncStatus"))

                Log.i(
                    TAG,
                    "WORKMANAGER_REMOTE_GREEN|path=patients/$uid/exerciseSessions/${completed.id}",
                )

                Log.i(
                    TAG,
                    "TELEREHAB_WORKMANAGER_SYNC_GREEN|sessionId=${completed.id}",
                )
            } catch (error: Throwable) {
                Log.e(
                    TAG,
                    "TELEREHAB_WORKMANAGER_SYNC_FAILED",
                    error,
                )
            } finally {
                finish()
            }
        }
    }

    private companion object {
        const val TAG =
            "TeleRehabWM"
    }
}
