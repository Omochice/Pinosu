package io.github.omochice.pinosu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.core.nip.nip65.Nip65EventParser
import io.github.omochice.pinosu.core.nip.nip65.Nip65EventParserImpl
import io.github.omochice.pinosu.core.nip.nip65.Nip65RelayListFetcher
import io.github.omochice.pinosu.core.nip.nip65.Nip65RelayListFetcherImpl

/**
 * Hilt module for Repository dependency injection
 *
 * Provides Repository interfaces with their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

  @Provides fun provideNip65EventParser(impl: Nip65EventParserImpl): Nip65EventParser = impl

  @Provides
  fun provideNip65RelayListFetcher(impl: Nip65RelayListFetcherImpl): Nip65RelayListFetcher = impl
}
