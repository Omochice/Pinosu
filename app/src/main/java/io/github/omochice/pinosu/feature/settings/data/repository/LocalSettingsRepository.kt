package io.github.omochice.pinosu.feature.settings.data.repository

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.data.local.LocalSettingsDataSource
import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
import io.github.omochice.pinosu.feature.settings.domain.repository.SettingsRepository
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

  override val themeModeFlow: StateFlow<ThemeMode> = localSettingsDataSource.themeModeFlow

  override fun getDisplayMode(): BookmarkDisplayMode = localSettingsDataSource.getDisplayMode()

  override fun setDisplayMode(mode: BookmarkDisplayMode) {
    localSettingsDataSource.setDisplayMode(mode)
  }

  override fun getThemeMode(): ThemeMode = localSettingsDataSource.getThemeMode()

  override fun setThemeMode(mode: ThemeMode) {
    localSettingsDataSource.setThemeMode(mode)
  }
}
