package io.github.omochice.pinosu.di

import dagger.Binds
import dagger.Module
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
abstract class RelayPoolModule {
  @Binds abstract fun bindRelayPool(impl: RelayPoolImpl): RelayPool
}
