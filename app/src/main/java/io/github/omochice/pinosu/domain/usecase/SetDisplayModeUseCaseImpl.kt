package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.SettingsRepository
import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import javax.inject.Inject

/**
 * Implementation of SetDisplayModeUseCase.
 *
 * Delegates to SettingsRepository for display mode persistence.
 */
class SetDisplayModeUseCaseImpl
@Inject
constructor(private val settingsRepository: SettingsRepository) : SetDisplayModeUseCase {

  /**
   * Save the bookmark display mode preference.
   *
   * @param mode Display mode to save
   */
  override fun invoke(mode: BookmarkDisplayMode) {
    settingsRepository.setDisplayMode(mode)
  }
}
