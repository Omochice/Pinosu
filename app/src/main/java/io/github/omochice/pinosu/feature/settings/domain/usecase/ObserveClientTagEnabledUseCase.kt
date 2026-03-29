package io.github.omochice.pinosu.feature.settings.domain.usecase

import kotlinx.coroutines.flow.StateFlow

/**
 * UseCase interface for observing client tag enabled preference changes.
 *
 * Provides reactive updates when client tag setting changes.
 */
interface ObserveClientTagEnabledUseCase {

  /**
   * Observe client tag enabled preference changes.
   *
   * @return StateFlow that emits client tag enabled changes
   */
  operator fun invoke(): StateFlow<Boolean>
}
