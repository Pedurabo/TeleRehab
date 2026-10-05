package com.signaldesk.telerehab.core.di

import com.signaldesk.telerehab.data.assignment.local.ExerciseAssignmentDao

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.signaldesk.telerehab.core.database.TeleRehabDatabase
import com.signaldesk.telerehab.core.database.dao.ExerciseSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTeleRehabDatabase(
        @ApplicationContext context: Context,
    ): TeleRehabDatabase =
        Room.databaseBuilder<TeleRehabDatabase>(
            context = context,
            name = "telerehab.db",
        )
            .setDriver(AndroidSQLiteDriver())
            .addMigrations(
                TeleRehabDatabase.MIGRATION_1_2,
                TeleRehabDatabase.MIGRATION_2_3,
                TeleRehabDatabase.MIGRATION_3_4,
                TeleRehabDatabase.MIGRATION_4_5,
            )
            .build()

    @Provides
    fun provideExerciseSessionDao(
        database: TeleRehabDatabase,
    ): ExerciseSessionDao =
        database.exerciseSessionDao()
    @Provides
    fun provideExerciseAssignmentDao(
        database: TeleRehabDatabase,
    ): ExerciseAssignmentDao =
        database.exerciseAssignmentDao()
}
