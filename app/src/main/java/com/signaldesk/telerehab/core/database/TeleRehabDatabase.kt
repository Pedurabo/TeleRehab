package com.signaldesk.telerehab.core.database

import androidx.sqlite.execSQL

import androidx.sqlite.SQLiteConnection

import androidx.room3.migration.Migration

import com.signaldesk.telerehab.data.assignment.local.ExerciseAssignmentDao

import com.signaldesk.telerehab.data.assignment.local.ExerciseAssignmentEntity

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.signaldesk.telerehab.core.database.dao.ExerciseSessionDao
import com.signaldesk.telerehab.core.database.entity.ExerciseSessionEntity

@Database(
    entities = [

        ExerciseAssignmentEntity::class,
ExerciseSessionEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class TeleRehabDatabase : RoomDatabase() {
    abstract fun exerciseSessionDao(): ExerciseSessionDao
    abstract fun exerciseAssignmentDao(): ExerciseAssignmentDao
    companion object {

        val MIGRATION_1_2 =
            object : Migration(
                1,
                2,
            ) {

                override suspend fun migrate(
                    connection: SQLiteConnection,
                ) {
                    connection.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS exercise_assignments (
                            id TEXT NOT NULL,
                            patientId TEXT NOT NULL,
                            exerciseId TEXT NOT NULL,
                            title TEXT NOT NULL,
                            instructions TEXT NOT NULL,
                            targetRepetitions INTEGER NOT NULL,
                            status TEXT NOT NULL,
                            PRIMARY KEY(id)
                        )
                        """.trimIndent(),
                    )
                }
            }

        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override suspend fun migrate(
                    connection: SQLiteConnection,
                ) {
                    connection.execSQL(
                        """
                        ALTER TABLE exercise_assignments
                        ADD COLUMN flexedAtOrBelowDegrees REAL
                        """.trimIndent(),
                    )

                    connection.execSQL(
                        """
                        ALTER TABLE exercise_assignments
                        ADD COLUMN extendedAtOrAboveDegrees REAL
                        """.trimIndent(),
                    )
                }
            }

        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override suspend fun migrate(
                    connection: SQLiteConnection,
                ) {
                    connection.execSQL(
                        """
                        ALTER TABLE exercise_sessions
                        ADD COLUMN completedRepetitions INTEGER
                        """.trimIndent(),
                    )

                    connection.execSQL(
                        """
                        ALTER TABLE exercise_sessions
                        ADD COLUMN minimumKneeAngleDegrees REAL
                        """.trimIndent(),
                    )

                    connection.execSQL(
                        """
                        ALTER TABLE exercise_sessions
                        ADD COLUMN maximumKneeAngleDegrees REAL
                        """.trimIndent(),
                    )
                }
            }
}
}
