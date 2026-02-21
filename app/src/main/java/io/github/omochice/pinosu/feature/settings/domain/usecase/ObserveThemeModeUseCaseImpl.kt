package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * Implementation of ObserveThemeModeUseCase.
 *
 * Provides reactive observation of theme mode preference changes via StateFlow.
 */
class ObserveThemeModeUseCaseImpl
@Inject
constructor(private val settingsRepository: SettingsRepository) : ObserveThemeModeUseCase {

  override fun invoke(): StateFlow<ThemeMode> = settingsRepository.themeModeFlow
}
