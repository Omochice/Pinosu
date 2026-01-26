package io.github.omochice.pinosu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.data.nip65.Nip65RelayListFetcher
import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.data.repository.BookmarkRepository
import io.github.omochice.pinosu.data.repository.SettingsRepository
import io.github.omochice.pinosu.domain.usecase.FetchRelayListUseCase
import io.github.omochice.pinosu.domain.usecase.FetchRelayListUseCaseImpl
import io.github.omochice.pinosu.domain.usecase.GetBookmarkListUseCase
import io.github.omochice.pinosu.domain.usecase.GetBookmarkListUseCaseImpl
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.domain.usecase.LogoutUseCase
import io.github.omochice.pinosu.domain.usecase.Nip55GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.Nip55LoginUseCase
import io.github.omochice.pinosu.domain.usecase.Nip55LogoutUseCase
import io.github.omochice.pinosu.domain.usecase.ObserveDisplayModeUseCase
import io.github.omochice.pinosu.domain.usecase.ObserveDisplayModeUseCaseImpl
import io.github.omochice.pinosu.domain.usecase.PostBookmarkUseCase
import io.github.omochice.pinosu.domain.usecase.PostBookmarkUseCaseImpl
import io.github.omochice.pinosu.domain.usecase.SetDisplayModeUseCase
import io.github.omochice.pinosu.domain.usecase.SetDisplayModeUseCaseImpl
import javax.inject.Singleton

/**
 * Hilt module for UseCase dependency injection
 *
 * Provides UseCase interfaces with their concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

  @Provides
  fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase =
      Nip55LoginUseCase(authRepository)

  @Provides
  fun provideLogoutUseCase(authRepository: AuthRepository): LogoutUseCase =
      Nip55LogoutUseCase(authRepository)

  @Provides
  fun provideGetLoginStateUseCase(authRepository: AuthRepository): GetLoginStateUseCase =
      Nip55GetLoginStateUseCase(authRepository)

  @Provides
  fun provideGetBookmarkListUseCase(
      bookmarkRepository: BookmarkRepository
  ): GetBookmarkListUseCase = GetBookmarkListUseCaseImpl(bookmarkRepository)

  @Provides
  fun provideFetchRelayListUseCase(
      fetcher: Nip65RelayListFetcher,
      localAuthDataSource: LocalAuthDataSource
  ): FetchRelayListUseCase = FetchRelayListUseCaseImpl(fetcher, localAuthDataSource)

  @Provides
  @Singleton
  fun providePostBookmarkUseCase(
      bookmarkRepository: BookmarkRepository,
      getLoginStateUseCase: GetLoginStateUseCase
  ): PostBookmarkUseCase = PostBookmarkUseCaseImpl(bookmarkRepository, getLoginStateUseCase)

  @Provides
  fun provideSetDisplayModeUseCase(settingsRepository: SettingsRepository): SetDisplayModeUseCase =
      SetDisplayModeUseCaseImpl(settingsRepository)

  @Provides
  fun provideObserveDisplayMode(settingsRepository: SettingsRepository): ObserveDisplayModeUseCase =
      ObserveDisplayModeUseCaseImpl(settingsRepository)
}
