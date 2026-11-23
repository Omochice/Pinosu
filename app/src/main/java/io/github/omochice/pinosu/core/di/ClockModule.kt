package io.github.omochice.pinosu.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

/** Hilt module providing the wall clock, so time-dependent code can be tested with a fixed clock */
@Module
@InstallIn(SingletonComponent::class)
object ClockModule {

  /** @return The system clock in UTC */
  @Provides fun provideClock(): Clock = Clock.systemUTC()
}
