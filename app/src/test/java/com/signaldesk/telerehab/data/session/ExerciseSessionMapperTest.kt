package com.signaldesk.telerehab.data.session

import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionStatus
import com.signaldesk.telerehab.domain.session.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ExerciseSessionMapperTest {

    @Test
    fun `domain session round trips through entity`() {
        val session =
            ExerciseSession(
                id = "session-1",
                assignmentId = "assignment-1",
                patientId = "patient-1",
                startedAt = Instant.parse("2026-10-04T12:00:00Z"),
                completedAt = Instant.parse("2026-10-04T12:15:00Z"),
                status = ExerciseSessionStatus.COMPLETED,
                syncStatus = SyncStatus.PENDING,
            )

        val restored =
            session
                .toEntity()
                .toDomain()

        assertEquals(session, restored)
    }

    @Test
    fun `null completion time survives mapping`() {
        val session =
            ExerciseSession(
                id = "session-2",
                assignmentId = "assignment-2",
                patientId = "patient-1",
                startedAt = Instant.parse("2026-10-04T12:00:00Z"),
                completedAt = null,
                status = ExerciseSessionStatus.IN_PROGRESS,
                syncStatus = SyncStatus.PENDING,
            )

        val entity =
            session.toEntity()

        assertNull(entity.completedAtEpochMillis)
        assertEquals(session, entity.toDomain())
    }
}
