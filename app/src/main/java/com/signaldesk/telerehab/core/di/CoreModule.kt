package com.signaldesk.telerehab.core.di

import com.signaldesk.telerehab.core.id.AppIdGenerator
import com.signaldesk.telerehab.core.id.UuidAppIdGenerator
import com.signaldesk.telerehab.core.time.AppClock
import com.signaldesk.telerehab.core.time.SystemAppClock
import com.signaldesk.telerehab.data.session.RoomExerciseSessionRepository
import com.signaldesk.telerehab.data.sync.FirestoreExerciseSessionSyncGateway
import com.signaldesk.telerehab.domain.session.ExerciseSessionRepository
import com.signaldesk.telerehab.domain.session.sync.PendingExerciseSessionSource
import com.signaldesk.telerehab.domain.session.sync.ExerciseSessionSyncGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {

    @Binds
    @Singleton
    abstract fun bindAppClock(
        implementation: SystemAppClock,
    ): AppClock
    @Binds
    @Singleton
    abstract fun bindExerciseSessionRepository(
        implementation: RoomExerciseSessionRepository,
    ): ExerciseSessionRepository
    @Binds
    @Singleton
    abstract fun bindAppIdGenerator(
        implementation: UuidAppIdGenerator,
    ): AppIdGenerator
    @Binds
    @Singleton
    abstract fun bindPendingExerciseSessionSource(
        implementation: RoomExerciseSessionRepository,
    ): PendingExerciseSessionSource
    @Binds
    @Singleton
    abstract fun bindExerciseSessionSyncGateway(
        implementation: FirestoreExerciseSessionSyncGateway,
    ): ExerciseSessionSyncGateway
}
