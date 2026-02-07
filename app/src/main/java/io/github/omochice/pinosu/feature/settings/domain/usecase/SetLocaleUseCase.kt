package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.model.AppLocale

/**
 * UseCase interface for changing the application locale.
 *
 * Provides abstraction for persisting locale setting.
 */
interface SetLocaleUseCase {

  /**
   * Apply the given application locale.
   *
   * @param locale [AppLocale] to apply
   */
  operator fun invoke(locale: AppLocale)
}
