package com.signaldesk.telerehab.di

import com.signaldesk.telerehab.domain.session.sync.SessionSyncScheduler
import com.signaldesk.telerehab.sync.WorkManagerSessionSyncScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncSchedulingModule {

    @Binds
    @Singleton
    abstract fun bindSessionSyncScheduler(
        implementation: WorkManagerSessionSyncScheduler,
    ): SessionSyncScheduler
}
