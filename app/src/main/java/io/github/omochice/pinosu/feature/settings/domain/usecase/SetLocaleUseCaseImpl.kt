package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.data.repository.SettingsRepository
import io.github.omochice.pinosu.feature.settings.domain.model.AppLocale
import javax.inject.Inject

/**
 * Implementation of SetLocaleUseCase.
 *
 * Delegates to SettingsRepository for locale persistence.
 */
class SetLocaleUseCaseImpl @Inject constructor(private val settingsRepository: SettingsRepository) :
    SetLocaleUseCase {

  override fun invoke(locale: AppLocale) {
    settingsRepository.setLocale(locale)
  }
}
