package io.github.omochice.pinosu.feature.settings.domain.usecase

import javax.inject.Inject

/**
 * Aggregates all settings-related use cases for ViewModel injection.
 *
 * Reduces constructor parameter count by grouping related use cases into a single dependency.
 */
data class SettingsUseCases
@Inject
constructor(
    val observeDisplayMode: ObserveDisplayModeUseCase,
    val setDisplayMode: SetDisplayModeUseCase,
    val observeThemeMode: ObserveThemeModeUseCase,
    val setThemeMode: SetThemeModeUseCase,
    val observeLanguageMode: ObserveLanguageModeUseCase,
    val setLanguageMode: SetLanguageModeUseCase,
    val observeBootstrapRelays: ObserveBootstrapRelaysUseCase,
    val setBootstrapRelays: SetBootstrapRelaysUseCase,
    val observeClientTagEnabled: ObserveClientTagEnabledUseCase,
    val setClientTagEnabled: SetClientTagEnabledUseCase,
)
