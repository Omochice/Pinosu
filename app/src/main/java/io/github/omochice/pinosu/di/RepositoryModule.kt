package io.github.omochice.pinosu.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.core.metadata.OkHttpUrlMetadataFetcher
import io.github.omochice.pinosu.core.metadata.UrlMetadataFetcher
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
 * list provider, bootstrap relay provider, and the URL metadata fetcher.
 */
@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

  @Binds fun bindNip01ProfileParser(impl: Nip01ProfileParserImpl): Nip01ProfileParser

  @Binds fun bindNip01ProfileFetcher(impl: Nip01ProfileFetcherImpl): Nip01ProfileFetcher

  @Binds fun bindNip65EventParser(impl: Nip65EventParserImpl): Nip65EventParser

  @Binds fun bindNip65RelayListFetcher(impl: Nip65RelayListFetcherImpl): Nip65RelayListFetcher

  @Binds fun bindRelayListProvider(impl: CachedRelayListProvider): RelayListProvider

  @Binds
  fun bindBootstrapRelayProvider(impl: SettingsBootstrapRelayProvider): BootstrapRelayProvider

  @Binds fun bindUrlMetadataFetcher(impl: OkHttpUrlMetadataFetcher): UrlMetadataFetcher
}
