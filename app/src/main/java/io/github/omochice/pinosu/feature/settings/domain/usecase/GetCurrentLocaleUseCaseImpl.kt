package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.data.repository.SettingsRepository
import io.github.omochice.pinosu.feature.settings.domain.model.AppLocale
import javax.inject.Inject

/**
 * Implementation of GetCurrentLocaleUseCase.
 *
 * Delegates to SettingsRepository for locale retrieval.
 */
class GetCurrentLocaleUseCaseImpl
@Inject
constructor(private val settingsRepository: SettingsRepository) : GetCurrentLocaleUseCase {

  override fun invoke(): AppLocale = settingsRepository.getLocale()
}
