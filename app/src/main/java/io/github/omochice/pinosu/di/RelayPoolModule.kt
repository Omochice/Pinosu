package io.github.omochice.pinosu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.data.relay.RelayPool
import io.github.omochice.pinosu.data.relay.RelayPoolImpl

/**
 * Hilt module for RelayPool dependency injection
 *
 * Separated from RepositoryModule to allow @TestInstallIn replacement in instrumented tests.
 */
@Module
@InstallIn(SingletonComponent::class)
object RelayPoolModule {
  @Provides fun provideRelayPool(impl: RelayPoolImpl): RelayPool = impl
}
