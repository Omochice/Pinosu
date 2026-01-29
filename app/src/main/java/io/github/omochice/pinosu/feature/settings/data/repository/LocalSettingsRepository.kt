package io.github.omochice.pinosu.feature.settings.data.repository

import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.data.local.LocalSettingsDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

/**
 * Local implementation of SettingsRepository.
 *
 * Uses LocalSettingsDataSource for persistence with reactive updates.
 */
@Singleton
class LocalSettingsRepository
@Inject
constructor(private val localSettingsDataSource: LocalSettingsDataSource) : SettingsRepository {
  override val displayModeFlow: StateFlow<BookmarkDisplayMode> =
      localSettingsDataSource.displayModeFlow

  override fun getDisplayMode(): BookmarkDisplayMode = localSettingsDataSource.getDisplayMode()

  override fun setDisplayMode(mode: BookmarkDisplayMode) {
    localSettingsDataSource.setDisplayMode(mode)
  }
}
