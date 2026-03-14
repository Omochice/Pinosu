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
import io.github.omochice.pinosu.core.relay.BootstrapRelayProvider
import io.github.omochice.pinosu.core.relay.RelayListProvider
import io.github.omochice.pinosu.feature.auth.data.CachedRelayListProvider
import io.github.omochice.pinosu.feature.settings.data.SettingsBootstrapRelayProvider

/**
 * Hilt module for NIP protocol dependency injection
 *
 * Provides NIP-01 profile metadata, NIP-65 relay list parser and fetcher implementations, relay
 * list provider, and bootstrap relay provider.
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

  @Provides fun provideRelayListProvider(impl: CachedRelayListProvider): RelayListProvider = impl

  @Provides
  fun provideBootstrapRelayProvider(impl: SettingsBootstrapRelayProvider): BootstrapRelayProvider =
      impl
}
