package io.github.omochice.pinosu.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import io.github.omochice.pinosu.core.logout.LogoutCleanup

/**
 * Hilt module declaring the set of [LogoutCleanup]s, so that it can be injected even when no
 * feature contributes to it
 */
@Module
@InstallIn(SingletonComponent::class)
interface LogoutCleanupModule {

  /** @return Every cleanup the features contribute */
  @Multibinds fun logoutCleanups(): Set<LogoutCleanup>
}
