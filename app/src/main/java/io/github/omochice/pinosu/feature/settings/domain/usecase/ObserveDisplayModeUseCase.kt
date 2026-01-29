package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import kotlinx.coroutines.flow.StateFlow

/**
 * UseCase interface for observing bookmark display mode preference changes.
 *
 * Provides reactive updates when display mode setting changes.
 */
interface ObserveDisplayModeUseCase {

  /**
   * Observe display mode preference changes.
   *
   * @return StateFlow that emits display mode changes
   */
  operator fun invoke(): StateFlow<BookmarkDisplayMode>
}
