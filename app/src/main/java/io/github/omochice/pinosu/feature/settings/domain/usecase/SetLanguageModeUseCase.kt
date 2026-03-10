package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.model.LanguageMode

/**
 * UseCase interface for saving language mode preference.
 *
 * Provides abstraction for persisting language mode setting.
 */
interface SetLanguageModeUseCase {

  /**
   * Save the language mode preference.
   *
   * @param mode Language mode to save
   */
  operator fun invoke(mode: LanguageMode)
}
