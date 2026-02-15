package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.data.repository.SettingsRepository
import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
import javax.inject.Inject

/**
 * Implementation of SetThemeModeUseCase.
 *
 * Delegates to SettingsRepository for theme mode persistence.
 */
class SetThemeModeUseCaseImpl
@Inject
constructor(private val settingsRepository: SettingsRepository) : SetThemeModeUseCase {

  override fun invoke(mode: ThemeMode) {
    settingsRepository.setThemeMode(mode)
  }
}
