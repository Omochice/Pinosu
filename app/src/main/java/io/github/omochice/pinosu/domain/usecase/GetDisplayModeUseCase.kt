package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode

/**
 * UseCase interface for retrieving bookmark display mode preference.
 *
 * Provides abstraction for accessing display mode setting.
 */
interface GetDisplayModeUseCase {

  /**
   * Retrieve the current bookmark display mode.
   *
   * @return Current display mode preference, defaults to List if not set
   */
  operator fun invoke(): BookmarkDisplayMode
}
