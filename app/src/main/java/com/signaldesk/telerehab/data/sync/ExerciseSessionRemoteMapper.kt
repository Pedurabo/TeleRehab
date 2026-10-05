package com.signaldesk.telerehab.data.sync

import com.signaldesk.telerehab.domain.session.ExerciseSession
import javax.inject.Inject

class ExerciseSessionRemoteMapper @Inject constructor() {

    fun toDocument(
        session: ExerciseSession,
    ): Map<String, Any?> =
        mapOf(
            "id" to session.id,
            "assignmentId" to session.assignmentId,
            "patientId" to session.patientId,
            "startedAtEpochMillis" to session.startedAt.toEpochMilli(),
            "completedAtEpochMillis" to session.completedAt?.toEpochMilli(),
            "sessionStatus" to session.status.name,

            "completedRepetitions" to session.metrics?.completedRepetitions,
            "minimumKneeAngleDegrees" to session.metrics?.minimumKneeAngleDegrees,
            "maximumKneeAngleDegrees" to session.metrics?.maximumKneeAngleDegrees,
        )
}
