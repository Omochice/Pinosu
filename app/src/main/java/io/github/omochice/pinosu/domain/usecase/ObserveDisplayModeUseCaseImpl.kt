package io.github.omochice.pinosu.domain.usecase

import io.github.omochice.pinosu.data.repository.SettingsRepository
import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * Implementation of ObserveDisplayModeUseCase.
 *
 * Provides reactive observation of display mode preference changes via StateFlow.
 */
class ObserveDisplayModeUseCaseImpl
@Inject
constructor(private val settingsRepository: SettingsRepository) : ObserveDisplayModeUseCase {

  override fun invoke(): StateFlow<BookmarkDisplayMode> {
    return settingsRepository.displayModeFlow
  }
}
