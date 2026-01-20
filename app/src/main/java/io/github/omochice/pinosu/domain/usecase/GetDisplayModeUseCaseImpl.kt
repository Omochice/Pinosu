package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.SettingsRepository
import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import javax.inject.Inject

/**
 * Implementation of GetDisplayModeUseCase.
 *
 * Delegates to SettingsRepository for display mode retrieval.
 *
 * @property settingsRepository Repository for settings data access
 */
class GetDisplayModeUseCaseImpl
@Inject
constructor(private val settingsRepository: SettingsRepository) : GetDisplayModeUseCase {

  /**
   * Retrieve the current bookmark display mode.
   *
   * @return Current display mode preference, defaults to List if not set
   */
  override fun invoke(): BookmarkDisplayMode {
    return settingsRepository.getDisplayMode()
  }
}
