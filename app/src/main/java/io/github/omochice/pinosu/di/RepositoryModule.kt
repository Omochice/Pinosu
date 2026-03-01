package io.github.omochice.pinosu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.core.nip.nip01.Nip01ProfileFetcher
import io.github.omochice.pinosu.core.nip.nip01.Nip01ProfileFetcherImpl
import io.github.omochice.pinosu.core.nip.nip01.Nip01ProfileParser
import io.github.omochice.pinosu.core.nip.nip01.Nip01ProfileParserImpl
import io.github.omochice.pinosu.core.nip.nip65.Nip65EventParser
import io.github.omochice.pinosu.core.nip.nip65.Nip65EventParserImpl
import io.github.omochice.pinosu.core.nip.nip65.Nip65RelayListFetcher
import io.github.omochice.pinosu.core.nip.nip65.Nip65RelayListFetcherImpl

/**
 * Hilt module for NIP protocol dependency injection
 *
 * Provides NIP-01 profile metadata and NIP-65 relay list parser and fetcher implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

  @Provides fun provideNip01ProfileParser(impl: Nip01ProfileParserImpl): Nip01ProfileParser = impl

  @Provides
  fun provideNip01ProfileFetcher(impl: Nip01ProfileFetcherImpl): Nip01ProfileFetcher = impl

  @Provides fun provideNip65EventParser(impl: Nip65EventParserImpl): Nip65EventParser = impl

  @Provides
  fun provideNip65RelayListFetcher(impl: Nip65RelayListFetcherImpl): Nip65RelayListFetcher = impl
}
