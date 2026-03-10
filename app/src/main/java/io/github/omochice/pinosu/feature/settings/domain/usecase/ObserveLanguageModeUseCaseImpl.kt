package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.model.LanguageMode
import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * Implementation of ObserveLanguageModeUseCase.
 *
 * Provides reactive observation of language mode preference changes via StateFlow.
 */
class ObserveLanguageModeUseCaseImpl
@Inject
constructor(private val settingsRepository: SettingsRepository) : ObserveLanguageModeUseCase {

  override fun invoke(): StateFlow<LanguageMode> = settingsRepository.languageModeFlow
}
