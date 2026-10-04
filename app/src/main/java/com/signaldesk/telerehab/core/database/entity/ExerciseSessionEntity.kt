package com.signaldesk.telerehab.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "exercise_sessions")
data class ExerciseSessionEntity(
    @PrimaryKey
    val id: String,
    val assignmentId: String,
    val patientId: String,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
    val sessionStatus: String,
    val syncStatus: String,
)
