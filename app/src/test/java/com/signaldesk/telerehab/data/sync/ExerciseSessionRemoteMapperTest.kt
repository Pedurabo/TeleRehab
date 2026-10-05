package com.signaldesk.telerehab.data.sync

import com.signaldesk.telerehab.domain.session.ExerciseSession
import com.signaldesk.telerehab.domain.session.ExerciseSessionMetrics
import com.signaldesk.telerehab.domain.session.ExerciseSessionStatus
import com.signaldesk.telerehab.domain.session.SyncStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ExerciseSessionRemoteMapperTest {

    private val mapper =
        ExerciseSessionRemoteMapper()

    @Test
    fun `maps durable session fields to Firestore document`() {
        val session =
            ExerciseSession(
                id = "session-123",
                assignmentId = "assignment-456",
                patientId = "patient-789",
                startedAt =
                    Instant.parse(
                        "2026-10-04T10:00:00Z",
                    ),
                completedAt =
                    Instant.parse(
                        "2026-10-04T10:15:00Z",
                    ),
                status =
                    ExerciseSessionStatus.COMPLETED,
                syncStatus =
                    SyncStatus.PENDING,
                metrics =
                    ExerciseSessionMetrics(
                        completedRepetitions = 8,
                        minimumKneeAngleDegrees = 82.5,
                        maximumKneeAngleDegrees = 171.0,
                    ),
            )

        val document =
            mapper.toDocument(session)

        assertEquals(
            "session-123",
            document["id"],
        )
        assertEquals(
            "assignment-456",
            document["assignmentId"],
        )
        assertEquals(
            "patient-789",
            document["patientId"],
        )
        assertEquals(
            Instant.parse(
                "2026-10-04T10:00:00Z",
            ).toEpochMilli(),
            document["startedAtEpochMillis"],
        )
        assertEquals(
            Instant.parse(
                "2026-10-04T10:15:00Z",
            ).toEpochMilli(),
            document["completedAtEpochMillis"],
        )
        assertEquals(
            "COMPLETED",
            document["sessionStatus"],
        )
        assertEquals(
            8,
            document["completedRepetitions"],
        )
        assertEquals(
            82.5,
            document["minimumKneeAngleDegrees"],
        )
        assertEquals(
            171.0,
            document["maximumKneeAngleDegrees"],
        )
    }

    @Test
    fun `does not upload local synchronization state`() {
        val session =
            ExerciseSession(
                id = "session-123",
                assignmentId = "assignment-456",
                patientId = "patient-789",
                startedAt =
                    Instant.parse(
                        "2026-10-04T10:00:00Z",
                    ),
                completedAt = null,
                status =
                    ExerciseSessionStatus.IN_PROGRESS,
                syncStatus =
                    SyncStatus.FAILED,
            )

        val document =
            mapper.toDocument(session)

        assertFalse(
            document.containsKey(
                "syncStatus",
            ),
        )

        assertEquals(
            setOf(
                "id",
                "assignmentId",
                "patientId",
                "startedAtEpochMillis",
                "completedAtEpochMillis",
                "sessionStatus",
                "completedRepetitions",
                "minimumKneeAngleDegrees",
                "maximumKneeAngleDegrees",
            ),
            document.keys,
        )

        assertEquals(
            null,
            document["completedRepetitions"],
        )
        assertEquals(
            null,
            document["minimumKneeAngleDegrees"],
        )
        assertEquals(
            null,
            document["maximumKneeAngleDegrees"],
        )
    }
}
