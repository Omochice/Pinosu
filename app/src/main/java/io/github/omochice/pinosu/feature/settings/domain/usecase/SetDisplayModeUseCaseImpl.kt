package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
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
