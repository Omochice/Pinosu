package io.github.omochice.pinosu.feature.settings.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.feature.settings.data.repository.LocalSettingsRepository
import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveDisplayModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveDisplayModeUseCaseImpl
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveLanguageModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveLanguageModeUseCaseImpl
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveThemeModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.ObserveThemeModeUseCaseImpl
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetDisplayModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetDisplayModeUseCaseImpl
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetLanguageModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetLanguageModeUseCaseImpl
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetThemeModeUseCase
import io.github.omochice.pinosu.feature.settings.domain.usecase.SetThemeModeUseCaseImpl

/**
 * Hilt module for appearance-related settings.
 *
 * Provides repository, display mode, theme, and language dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppearanceModule {

  @Provides fun provideSettingsRepository(impl: LocalSettingsRepository): SettingsRepository = impl

  @Provides
  fun provideSetDisplayModeUseCase(impl: SetDisplayModeUseCaseImpl): SetDisplayModeUseCase = impl

  @Provides
  fun provideObserveDisplayMode(impl: ObserveDisplayModeUseCaseImpl): ObserveDisplayModeUseCase =
      impl

  @Provides
  fun provideSetThemeModeUseCase(impl: SetThemeModeUseCaseImpl): SetThemeModeUseCase = impl

  @Provides
  fun provideObserveThemeModeUseCase(impl: ObserveThemeModeUseCaseImpl): ObserveThemeModeUseCase =
      impl

  @Provides
  fun provideSetLanguageModeUseCase(impl: SetLanguageModeUseCaseImpl): SetLanguageModeUseCase = impl

  @Provides
  fun provideObserveLanguageMode(impl: ObserveLanguageModeUseCaseImpl): ObserveLanguageModeUseCase =
      impl
}
