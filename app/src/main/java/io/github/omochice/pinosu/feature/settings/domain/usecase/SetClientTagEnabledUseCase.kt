package io.github.omochice.pinosu.feature.settings.domain.usecase

/**
 * UseCase interface for saving client tag enabled preference.
 *
 * Provides abstraction for persisting client tag setting.
 */
interface SetClientTagEnabledUseCase {

  /**
   * Save the client tag enabled preference.
   *
   * @param enabled Whether to include client tag in published events
   */
  operator fun invoke(enabled: Boolean)
}
