package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode

/**
 * UseCase interface for saving theme mode preference.
 *
 * Provides abstraction for persisting theme mode setting.
 */
interface SetThemeModeUseCase {

  /**
   * Save the theme mode preference.
   *
   * @param mode Theme mode to save
   */
  operator fun invoke(mode: ThemeMode)
}
