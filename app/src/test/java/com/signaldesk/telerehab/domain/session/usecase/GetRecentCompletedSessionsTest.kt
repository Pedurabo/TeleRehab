package com.signaldesk.telerehab.domain.session.usecase

import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionRepository
import com.signaldesk.telerehab.domain.session.ExerciseSessionStatus
import com.signaldesk.telerehab.domain.session.SyncStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class GetRecentCompletedSessionsTest {

    @Test
    fun `returns repository history for requested patient and limit`() =
        runBlocking {
            val expected =
                listOf(
                    completedSession(
                        id = "session-2",
                        completedAt = "2026-10-04T16:30:00Z",
                    ),
                    completedSession(
                        id = "session-1",
                        completedAt = "2026-10-04T16:00:00Z",
                    ),
                )

            val repository =
                RecordingRepository(expected)

            val useCase =
                GetRecentCompletedSessions(repository)

            val result =
                useCase(
                    patientId = "patient-7",
                    limit = 2,
                )

            assertEquals(expected, result)
            assertEquals("patient-7", repository.requestedPatientId)
            assertEquals(2, repository.requestedLimit)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `blank patient id is rejected`() {
        runBlocking {
            val useCase =
                GetRecentCompletedSessions(
                    RecordingRepository(emptyList()),
                )

            useCase(
                patientId = " ",
                limit = 10,
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero limit is rejected`() {
        runBlocking {
            val useCase =
                GetRecentCompletedSessions(
                    RecordingRepository(emptyList()),
                )

            useCase(
                patientId = "patient-7",
                limit = 0,
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative limit is rejected`() {
        runBlocking {
            val useCase =
                GetRecentCompletedSessions(
                    RecordingRepository(emptyList()),
                )

            useCase(
                patientId = "patient-7",
                limit = -1,
            )
        }
    }

    private fun completedSession(
        id: String,
        completedAt: String,
    ): ExerciseSession =
        ExerciseSession(
            id = id,
            assignmentId = "assignment-$id",
            patientId = "patient-7",
            startedAt = Instant.parse(
                "2026-10-04T15:00:00Z",
            ),
            completedAt = Instant.parse(completedAt),
            status = ExerciseSessionStatus.COMPLETED,
            syncStatus = SyncStatus.PENDING,
        )

    private class RecordingRepository(
        private val sessions: List<ExerciseSession>,
    ) : ExerciseSessionRepository {

        var requestedPatientId: String? = null
        var requestedLimit: Int? = null

        override suspend fun save(
            session: ExerciseSession,
        ) = Unit

        override suspend fun findById(
            sessionId: String,
        ): ExerciseSession? = null

        override suspend fun findRecentCompletedByPatient(
            patientId: String,
            limit: Int,
        ): List<ExerciseSession> {
            requestedPatientId = patientId
            requestedLimit = limit
            return sessions
        }
    }
}
