package io.github.omochice.pinosu.feature.settings.data.repository

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.data.local.LocalSettingsDataSource
import io.github.omochice.pinosu.feature.settings.domain.model.LanguageMode
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

  override val languageModeFlow: StateFlow<LanguageMode> = localSettingsDataSource.languageModeFlow

  override val clientTagEnabledFlow: StateFlow<Boolean> =
      localSettingsDataSource.clientTagEnabledFlow

  override val bootstrapRelaysFlow: StateFlow<Set<String>> =
      localSettingsDataSource.bootstrapRelaysFlow

  override fun setDisplayMode(mode: BookmarkDisplayMode) {
    localSettingsDataSource.setDisplayMode(mode)
  }

  override fun setThemeMode(mode: ThemeMode) {
    localSettingsDataSource.setThemeMode(mode)
  }

  override fun setLanguageMode(mode: LanguageMode) {
    localSettingsDataSource.setLanguageMode(mode)
  }

  override fun setClientTagEnabled(enabled: Boolean) {
    localSettingsDataSource.setClientTagEnabled(enabled)
  }

  override fun setBootstrapRelays(relays: Set<String>) {
    localSettingsDataSource.setBootstrapRelays(relays)
  }
}
