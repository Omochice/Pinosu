package io.github.omochice.pinosu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.data.metadata.OkHttpUrlMetadataFetcher
import io.github.omochice.pinosu.data.metadata.UrlMetadataFetcher
import io.github.omochice.pinosu.data.nip65.Nip65EventParser
import io.github.omochice.pinosu.data.nip65.Nip65EventParserImpl
import io.github.omochice.pinosu.data.nip65.Nip65RelayListFetcher
import io.github.omochice.pinosu.data.nip65.Nip65RelayListFetcherImpl
import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.data.repository.BookmarkRepository
import io.github.omochice.pinosu.data.repository.LocalSettingsRepository
import io.github.omochice.pinosu.data.repository.Nip55AuthRepository
import io.github.omochice.pinosu.data.repository.RelayBookmarkRepository
import io.github.omochice.pinosu.data.repository.SettingsRepository
import javax.inject.Singleton

/**
 * Hilt module for Repository dependency injection
 *
 * Provides Repository interfaces with their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

  @Provides fun provideAuthRepository(impl: Nip55AuthRepository): AuthRepository = impl

  @Provides
  @Singleton
  fun provideBookmarkRepository(impl: RelayBookmarkRepository): BookmarkRepository = impl

  @Provides
  @Singleton
  fun provideUrlMetadataFetcher(impl: OkHttpUrlMetadataFetcher): UrlMetadataFetcher = impl

  @Provides
  @Singleton
  fun provideNip65EventParser(impl: Nip65EventParserImpl): Nip65EventParser = impl

  @Provides
  @Singleton
  fun provideNip65RelayListFetcher(impl: Nip65RelayListFetcherImpl): Nip65RelayListFetcher = impl

  @Provides
  @Singleton
  fun provideSettingsRepository(impl: LocalSettingsRepository): SettingsRepository = impl
}
