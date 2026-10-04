package com.signaldesk.telerehab.domain.session.usecase

import com.signaldesk.telerehab.core.id.AppIdGenerator
import com.signaldesk.telerehab.core.time.AppClock
import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionRepository
import com.signaldesk.telerehab.domain.session.ExerciseSessionStatus
import com.signaldesk.telerehab.domain.session.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class StartExerciseSessionTest {

    @Test
    fun `starting a session creates offline pending session and saves it`() {
        val expectedNow =
            Instant.parse("2026-10-04T16:00:00Z")

        val repository =
            RecordingRepository()

        val useCase =
            StartExerciseSession(
                repository = repository,
                clock = FixedClock(expectedNow),
                idGenerator = FixedIdGenerator("session-123"),
            )

        val result =
            kotlinx.coroutines.runBlocking {
                useCase(
                    assignmentId = "assignment-42",
                    patientId = "patient-7",
                )
            }

        assertEquals("session-123", result.id)
        assertEquals("assignment-42", result.assignmentId)
        assertEquals("patient-7", result.patientId)
        assertEquals(expectedNow, result.startedAt)
        assertNull(result.completedAt)
        assertEquals(
            ExerciseSessionStatus.IN_PROGRESS,
            result.status,
        )
        assertEquals(
            SyncStatus.PENDING,
            result.syncStatus,
        )
        assertEquals(result, repository.saved)
    }

    private class FixedClock(
        private val instant: Instant,
    ) : AppClock {
        override fun now(): Instant = instant
    }

    private class FixedIdGenerator(
        private val id: String,
    ) : AppIdGenerator {
        override fun newId(): String = id
    }

    private class RecordingRepository : ExerciseSessionRepository {

        var saved: ExerciseSession? = null

        override suspend fun save(
            session: ExerciseSession,
        ) {
            saved = session
        }

        override suspend fun findById(
            sessionId: String,
        ): ExerciseSession? = null
    }
    @Test(expected = IllegalArgumentException::class)
    fun `blank assignment id is rejected`() {
        val useCase =
            StartExerciseSession(
                repository = RecordingRepository(),
                clock = FixedClock(
                    Instant.parse("2026-10-04T16:00:00Z"),
                ),
                idGenerator = FixedIdGenerator("session-123"),
            )

        kotlinx.coroutines.runBlocking {
            useCase(
                assignmentId = " ",
                patientId = "patient-7",
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank patient id is rejected`() {
        val useCase =
            StartExerciseSession(
                repository = RecordingRepository(),
                clock = FixedClock(
                    Instant.parse("2026-10-04T16:00:00Z"),
                ),
                idGenerator = FixedIdGenerator("session-123"),
            )

        kotlinx.coroutines.runBlocking {
            useCase(
                assignmentId = "assignment-42",
                patientId = "",
            )
        }
    }
}
