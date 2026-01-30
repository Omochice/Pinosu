package io.github.omochice.pinosu.feature.settings.domain.usecase

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.data.repository.SettingsRepository
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

  override fun invoke(): StateFlow<BookmarkDisplayMode> = settingsRepository.displayModeFlow
}
