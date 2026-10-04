package com.signaldesk.telerehab.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.signaldesk.telerehab.core.database.dao.ExerciseSessionDao
import com.signaldesk.telerehab.core.database.entity.ExerciseSessionEntity

@Database(
    entities = [
        ExerciseSessionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class TeleRehabDatabase : RoomDatabase() {
    abstract fun exerciseSessionDao(): ExerciseSessionDao
}
