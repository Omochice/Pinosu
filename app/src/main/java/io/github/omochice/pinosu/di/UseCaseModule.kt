package io.github.omochice.pinosu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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

  @Provides fun provideLoginUseCase(impl: Nip55LoginUseCase): LoginUseCase = impl

  @Provides fun provideLogoutUseCase(impl: Nip55LogoutUseCase): LogoutUseCase = impl

  @Provides
  fun provideGetLoginStateUseCase(impl: Nip55GetLoginStateUseCase): GetLoginStateUseCase = impl

  @Provides
  fun provideGetBookmarkListUseCase(impl: GetBookmarkListUseCaseImpl): GetBookmarkListUseCase = impl

  @Provides
  fun provideFetchRelayListUseCase(impl: FetchRelayListUseCaseImpl): FetchRelayListUseCase = impl

  @Provides
  @Singleton
  fun providePostBookmarkUseCase(impl: PostBookmarkUseCaseImpl): PostBookmarkUseCase = impl

  @Provides
  fun provideSetDisplayModeUseCase(impl: SetDisplayModeUseCaseImpl): SetDisplayModeUseCase = impl

  @Provides
  fun provideObserveDisplayModeUseCase(
      impl: ObserveDisplayModeUseCaseImpl
  ): ObserveDisplayModeUseCase = impl
}
