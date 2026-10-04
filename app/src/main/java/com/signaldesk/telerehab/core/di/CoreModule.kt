package com.signaldesk.telerehab.core.di

import com.signaldesk.telerehab.core.time.AppClock
import com.signaldesk.telerehab.core.time.SystemAppClock
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
}
