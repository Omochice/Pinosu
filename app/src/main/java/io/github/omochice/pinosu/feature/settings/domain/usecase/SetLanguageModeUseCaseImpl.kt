package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.model.LanguageMode
import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Implementation of SetLanguageModeUseCase.
 *
 * Delegates to SettingsRepository for language mode persistence.
 */
class SetLanguageModeUseCaseImpl
@Inject
constructor(private val settingsRepository: SettingsRepository) : SetLanguageModeUseCase {

  override fun invoke(mode: LanguageMode) {
    settingsRepository.setLanguageMode(mode)
  }
}
