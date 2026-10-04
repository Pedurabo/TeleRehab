package com.signaldesk.telerehab.domain.session.usecase

import com.signaldesk.telerehab.core.time.AppClock
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionRepository
import com.signaldesk.telerehab.domain.session.ExerciseSessionStatus
import com.signaldesk.telerehab.domain.session.SyncStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.time.Instant

class CompleteExerciseSessionTest {

    @Test
    fun `completing in progress session stamps time and keeps sync pending`() =
        runBlocking {
            val startedAt =
                Instant.parse("2026-10-04T16:00:00Z")

            val completedAt =
                Instant.parse("2026-10-04T16:30:00Z")

            val existing =
                ExerciseSession(
                    id = "session-1",
                    assignmentId = "assignment-1",
                    patientId = "patient-1",
                    startedAt = startedAt,
                    completedAt = null,
                    status = ExerciseSessionStatus.IN_PROGRESS,
                    syncStatus = SyncStatus.PENDING,
                )

            val repository =
                RecordingRepository(existing)

            val useCase =
                CompleteExerciseSession(
                    repository = repository,
                    clock = FixedClock(completedAt),
                )

            val result =
                useCase("session-1")

            assertEquals(existing.id, result.id)
            assertEquals(existing.assignmentId, result.assignmentId)
            assertEquals(existing.patientId, result.patientId)
            assertEquals(startedAt, result.startedAt)
            assertEquals(completedAt, result.completedAt)
            assertEquals(
                ExerciseSessionStatus.COMPLETED,
                result.status,
            )
            assertEquals(
                SyncStatus.PENDING,
                result.syncStatus,
            )
            assertEquals(result, repository.saved)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `blank session id is rejected`() {
        runBlocking {
            val useCase =
                CompleteExerciseSession(
                    repository = RecordingRepository(null),
                    clock = FixedClock(
                        Instant.parse("2026-10-04T16:30:00Z"),
                    ),
                )

            useCase(" ")
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `missing session is rejected`() {
        runBlocking {
            val useCase =
                CompleteExerciseSession(
                    repository = RecordingRepository(null),
                    clock = FixedClock(
                        Instant.parse("2026-10-04T16:30:00Z"),
                    ),
                )

            useCase("missing-session")
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `completed session cannot be completed again`() {
        runBlocking {
            val existing =
                ExerciseSession(
                    id = "session-1",
                    assignmentId = "assignment-1",
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

            val repository =
                RecordingRepository(existing)

            val useCase =
                CompleteExerciseSession(
                    repository = repository,
                    clock = FixedClock(
                        Instant.parse("2026-10-04T16:45:00Z"),
                    ),
                )

            useCase("session-1")
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `interrupted session cannot be completed`() {
        runBlocking {
            val existing =
                ExerciseSession(
                    id = "session-1",
                    assignmentId = "assignment-1",
                    patientId = "patient-1",
                    startedAt = Instant.parse(
                        "2026-10-04T16:00:00Z",
                    ),
                    completedAt = null,
                    status = ExerciseSessionStatus.INTERRUPTED,
                    syncStatus = SyncStatus.PENDING,
                )

            val repository =
                RecordingRepository(existing)

            val useCase =
                CompleteExerciseSession(
                    repository = repository,
                    clock = FixedClock(
                        Instant.parse("2026-10-04T16:45:00Z"),
                    ),
                )

            useCase("session-1")
        }
    }

    @Test
    fun `invalid transition does not persist replacement session`() =
        runBlocking {
            val existing =
                ExerciseSession(
                    id = "session-1",
                    assignmentId = "assignment-1",
                    patientId = "patient-1",
                    startedAt = Instant.parse(
                        "2026-10-04T16:00:00Z",
                    ),
                    completedAt = null,
                    status = ExerciseSessionStatus.CANCELLED,
                    syncStatus = SyncStatus.PENDING,
                )

            val repository =
                RecordingRepository(existing)

            val useCase =
                CompleteExerciseSession(
                    repository = repository,
                    clock = FixedClock(
                        Instant.parse("2026-10-04T16:45:00Z"),
                    ),
                )

            try {
                useCase("session-1")
            } catch (_: IllegalStateException) {
                // Expected.
            }

            assertSame(existing, repository.current)
            assertEquals(null, repository.saved)
        }

    private class FixedClock(
        private val instant: Instant,
    ) : AppClock {
        override fun now(): Instant = instant
    }

    private class RecordingRepository(
        initial: ExerciseSession?,
    ) : ExerciseSessionRepository {

        var current: ExerciseSession? = initial
        var saved: ExerciseSession? = null

        override suspend fun save(
            session: ExerciseSession,
        ) {
            saved = session
            current = session
        }

        override suspend fun findById(
            sessionId: String,
        ): ExerciseSession? =
            current?.takeIf {
                it.id == sessionId
            }
    }
    @Test
    fun `completion before start time is rejected without persisting`() =
        runBlocking {
            val existing =
                ExerciseSession(
                    id = "session-1",
                    assignmentId = "assignment-1",
                    patientId = "patient-1",
                    startedAt = Instant.parse(
                        "2026-10-04T16:30:00Z",
                    ),
                    completedAt = null,
                    status = ExerciseSessionStatus.IN_PROGRESS,
                    syncStatus = SyncStatus.PENDING,
                )

            val repository =
                RecordingRepository(existing)

            val useCase =
                CompleteExerciseSession(
                    repository = repository,
                    clock = FixedClock(
                        Instant.parse("2026-10-04T16:00:00Z"),
                    ),
                )

            try {
                useCase("session-1")
            } catch (_: IllegalStateException) {
                // Expected.
            }

            assertSame(existing, repository.current)
            assertEquals(null, repository.saved)
        }
}
