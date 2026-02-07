package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.model.AppLocale

/**
 * UseCase interface for retrieving the current application locale.
 *
 * Provides abstraction for reading the current locale setting.
 */
interface GetCurrentLocaleUseCase {

  /**
   * Retrieve the current application locale.
   *
   * @return Current [AppLocale]
   */
  operator fun invoke(): AppLocale
}
