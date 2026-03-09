package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.model.LanguageMode
import kotlinx.coroutines.flow.StateFlow

/**
 * UseCase interface for observing language mode preference changes.
 *
 * Provides reactive updates when language mode setting changes.
 */
interface ObserveLanguageModeUseCase {

  /**
   * Observe language mode preference changes.
   *
   * @return StateFlow that emits language mode changes
   */
  operator fun invoke(): StateFlow<LanguageMode>
}
