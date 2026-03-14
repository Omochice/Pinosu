package io.github.omochice.pinosu.feature.settings.domain.usecase

/**
 * UseCase interface for saving bootstrap relay preferences.
 *
 * Provides abstraction for persisting bootstrap relay settings.
 */
interface SetBootstrapRelaysUseCase {

  /**
   * Save the bootstrap relay preferences.
   *
   * @param relays Set of relay URLs to save
   */
  operator fun invoke(relays: Set<String>)
}
