package io.github.omochice.pinosu.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.core.relay.RelayPool
import io.github.omochice.pinosu.core.relay.RelayPoolImpl

/**
 * Hilt module for RelayPool dependency injection
 *
 * Separated from RepositoryModule to allow @TestInstallIn replacement in instrumented tests.
 */
@Module
@InstallIn(SingletonComponent::class)
interface RelayPoolModule {
  @Binds fun bindRelayPool(impl: RelayPoolImpl): RelayPool
}
