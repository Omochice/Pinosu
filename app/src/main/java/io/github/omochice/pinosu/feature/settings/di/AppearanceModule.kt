package io.github.omochice.pinosu.feature.settings.di

import dagger.Binds
import dagger.Module
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
interface AppearanceModule {

  @Binds fun bindSettingsRepository(impl: LocalSettingsRepository): SettingsRepository

  @Binds fun bindSetDisplayModeUseCase(impl: SetDisplayModeUseCaseImpl): SetDisplayModeUseCase

  @Binds fun bindObserveDisplayMode(impl: ObserveDisplayModeUseCaseImpl): ObserveDisplayModeUseCase

  @Binds fun bindSetThemeModeUseCase(impl: SetThemeModeUseCaseImpl): SetThemeModeUseCase

  @Binds fun bindObserveThemeModeUseCase(impl: ObserveThemeModeUseCaseImpl): ObserveThemeModeUseCase

  @Binds fun bindSetLanguageModeUseCase(impl: SetLanguageModeUseCaseImpl): SetLanguageModeUseCase

  @Binds
  fun bindObserveLanguageMode(impl: ObserveLanguageModeUseCaseImpl): ObserveLanguageModeUseCase
}
