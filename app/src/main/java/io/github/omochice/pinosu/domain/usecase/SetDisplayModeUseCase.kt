package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode

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
