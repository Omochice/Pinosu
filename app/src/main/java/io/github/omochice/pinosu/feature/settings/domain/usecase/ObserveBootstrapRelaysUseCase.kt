package io.github.omochice.pinosu.feature.settings.domain.usecase

import kotlinx.coroutines.flow.StateFlow

/**
 * UseCase interface for observing bootstrap relay preference changes.
 *
 * Provides reactive updates when bootstrap relay settings change.
 */
interface ObserveBootstrapRelaysUseCase {

  /**
   * Observe bootstrap relay preference changes.
   *
   * @return StateFlow that emits bootstrap relay URL sets
   */
  operator fun invoke(): StateFlow<Set<String>>
}
