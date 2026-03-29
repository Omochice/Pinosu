package io.github.omochice.pinosu.feature.settings.domain.repository

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.LanguageMode
import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for application settings.
 *
 * Provides abstraction layer for settings data access with reactive updates.
 */
interface SettingsRepository {
  /** Observable StateFlow of display mode preference for reactive updates */
  val displayModeFlow: StateFlow<BookmarkDisplayMode>

  /** Observable StateFlow of theme mode preference for reactive updates */
  val themeModeFlow: StateFlow<ThemeMode>

  /** Observable StateFlow of language mode preference for reactive updates */
  val languageModeFlow: StateFlow<LanguageMode>

  /** Observable StateFlow of client tag enabled preference for reactive updates */
  val clientTagEnabledFlow: StateFlow<Boolean>

  /** Observable StateFlow of user-configured bootstrap relay URLs for reactive updates */
  val bootstrapRelaysFlow: StateFlow<Set<String>>

  /**
   * Save bookmark display mode preference.
   *
   * @param mode Display mode to save
   */
  fun setDisplayMode(mode: BookmarkDisplayMode)

  /**
   * Save theme mode preference.
   *
   * @param mode Theme mode to save
   */
  fun setThemeMode(mode: ThemeMode)

  /**
   * Save language mode preference.
   *
   * @param mode Language mode to save
   */
  fun setLanguageMode(mode: LanguageMode)

  /**
   * Save client tag enabled preference.
   *
   * @param enabled Whether to include client tag in published events
   */
  fun setClientTagEnabled(enabled: Boolean)

  /**
   * Save user-configured bootstrap relay URLs.
   *
   * @param relays Set of relay URLs to save
   */
  fun setBootstrapRelays(relays: Set<String>)
}
