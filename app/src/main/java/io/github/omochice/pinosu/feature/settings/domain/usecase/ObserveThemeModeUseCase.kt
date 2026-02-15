package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

/**
 * UseCase interface for observing theme mode preference changes.
 *
 * Provides reactive updates when theme mode setting changes.
 */
interface ObserveThemeModeUseCase {

  /**
   * Observe theme mode preference changes.
   *
   * @return StateFlow that emits theme mode changes
   */
  operator fun invoke(): StateFlow<ThemeMode>
}
