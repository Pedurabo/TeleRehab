package com.signaldesk.telerehab.domain.session.sync

import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionRepository
import com.signaldesk.telerehab.domain.session.ExerciseSessionStatus
import com.signaldesk.telerehab.domain.session.SyncStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class SyncPendingSessionsTest {

    @Test
    fun `successful remote upsert marks session synced`() =
        runBlocking {
            val session = pendingSession("session-1")
            val repository = RecordingRepository()

            val useCase =
                SyncPendingSessions(
                    pendingSource = FixedPendingSource(
                        listOf(session),
                    ),
                    repository = repository,
                    gateway = FixedGateway(
                        SessionSyncResult.SUCCESS,
                    ),
                )

            val result = useCase()

            assertEquals(
                SyncBatchResult.COMPLETED,
                result,
            )
            assertEquals(
                SyncStatus.SYNCED,
                repository.saved.single().syncStatus,
            )
        }

    @Test
    fun `retryable failure keeps session pending and requests retry`() =
        runBlocking {
            val session = pendingSession("session-1")
            val repository = RecordingRepository()

            val useCase =
                SyncPendingSessions(
                    pendingSource = FixedPendingSource(
                        listOf(session),
                    ),
                    repository = repository,
                    gateway = FixedGateway(
                        SessionSyncResult.RETRYABLE_FAILURE,
                    ),
                )

            val result = useCase()

            assertEquals(
                SyncBatchResult.RETRY,
                result,
            )
            assertEquals(
                emptyList<ExerciseSession>(),
                repository.saved,
            )
        }

    @Test
    fun `permanent failure marks session failed`() =
        runBlocking {
            val session = pendingSession("session-1")
            val repository = RecordingRepository()

            val useCase =
                SyncPendingSessions(
                    pendingSource = FixedPendingSource(
                        listOf(session),
                    ),
                    repository = repository,
                    gateway = FixedGateway(
                        SessionSyncResult.PERMANENT_FAILURE,
                    ),
                )

            val result = useCase()

            assertEquals(
                SyncBatchResult.COMPLETED,
                result,
            )
            assertEquals(
                SyncStatus.FAILED,
                repository.saved.single().syncStatus,
            )
        }

    @Test(expected = IllegalArgumentException::class)
    fun `non positive batch limit is rejected`() {
        runBlocking {
            SyncPendingSessions(
                pendingSource = FixedPendingSource(
                    emptyList(),
                ),
                repository = RecordingRepository(),
                gateway = FixedGateway(
                    SessionSyncResult.SUCCESS,
                ),
            )(0)
        }
    }

    private fun pendingSession(
        id: String,
    ): ExerciseSession =
        ExerciseSession(
            id = id,
            assignmentId = "assignment-$id",
            patientId = "patient-1",
            startedAt = Instant.parse(
                "2026-10-04T16:00:00Z",
            ),
            completedAt = Instant.parse(
                "2026-10-04T16:30:00Z",
            ),
            status = ExerciseSessionStatus.COMPLETED,
            syncStatus = SyncStatus.PENDING,
        )

    private class FixedPendingSource(
        private val sessions: List<ExerciseSession>,
    ) : PendingExerciseSessionSource {

        override suspend fun findPendingSessions(
            limit: Int,
        ): List<ExerciseSession> =
            sessions.take(limit)
    }

    private class FixedGateway(
        private val result: SessionSyncResult,
    ) : ExerciseSessionSyncGateway {

        override suspend fun upsertSession(
            session: ExerciseSession,
        ): SessionSyncResult =
            result
    }

    private class RecordingRepository :
        ExerciseSessionRepository {

        val saved =
            mutableListOf<ExerciseSession>()

        override suspend fun save(
            session: ExerciseSession,
        ) {
            saved += session
        }

        override suspend fun findById(
            sessionId: String,
        ): ExerciseSession? = null

        override suspend fun findRecentCompletedByPatient(
            patientId: String,
            limit: Int,
        ): List<ExerciseSession> =
            emptyList()
    }
}
