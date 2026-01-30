package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode

/**
 * UseCase interface for saving bookmark display mode preference.
 *
 * Provides abstraction for persisting display mode setting.
 */
interface SetDisplayModeUseCase {

  /**
   * Save the bookmark display mode preference.
   *
   * @param mode Display mode to save
   */
  operator fun invoke(mode: BookmarkDisplayMode)
}
