package io.github.omochice.pinosu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.data.local.LocalSettingsDataSource
import io.github.omochice.pinosu.data.metadata.OkHttpUrlMetadataFetcher
import io.github.omochice.pinosu.data.metadata.UrlMetadataFetcher
import io.github.omochice.pinosu.data.nip55.Nip55SignerClient
import io.github.omochice.pinosu.data.nip65.Nip65EventParser
import io.github.omochice.pinosu.data.nip65.Nip65EventParserImpl
import io.github.omochice.pinosu.data.nip65.Nip65RelayListFetcher
import io.github.omochice.pinosu.data.nip65.Nip65RelayListFetcherImpl
import io.github.omochice.pinosu.data.relay.RelayPool
import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.data.repository.BookmarkRepository
import io.github.omochice.pinosu.data.repository.LocalSettingsRepository
import io.github.omochice.pinosu.data.repository.Nip55AuthRepository
import io.github.omochice.pinosu.data.repository.RelayBookmarkRepository
import io.github.omochice.pinosu.data.repository.SettingsRepository
import javax.inject.Singleton
import okhttp3.OkHttpClient

/**
 * Hilt module for Repository dependency injection
 *
 * Provides Repository interfaces with their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

  @Provides
  fun provideAuthRepository(
      nip55SignerClient: Nip55SignerClient,
      localAuthDataSource: LocalAuthDataSource
  ): AuthRepository = Nip55AuthRepository(nip55SignerClient, localAuthDataSource)

  @Provides
  @Singleton
  fun provideBookmarkRepository(
      relayPool: RelayPool,
      localAuthDataSource: LocalAuthDataSource,
      urlMetadataFetcher: UrlMetadataFetcher
  ): BookmarkRepository =
      RelayBookmarkRepository(relayPool, localAuthDataSource, urlMetadataFetcher)

  @Provides
  @Singleton
  fun provideUrlMetadataFetcher(okHttpClient: OkHttpClient): UrlMetadataFetcher =
      OkHttpUrlMetadataFetcher(okHttpClient)

  @Provides fun provideNip65EventParser(): Nip65EventParser = Nip65EventParserImpl()

  @Provides
  @Singleton
  fun provideNip65RelayListFetcher(
      relayPool: RelayPool,
      parser: Nip65EventParser
  ): Nip65RelayListFetcher = Nip65RelayListFetcherImpl(relayPool, parser)

  @Provides
  @Singleton
  fun provideSettingsRepository(
      localSettingsDataSource: LocalSettingsDataSource
  ): SettingsRepository = LocalSettingsRepository(localSettingsDataSource)
}
