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

  /** Observable StateFlow of user-configured bootstrap relay URLs for reactive updates */
  val bootstrapRelaysFlow: StateFlow<Set<String>>

  /**
   * Retrieve bookmark display mode preference.
   *
   * @return Stored display mode, defaults to List if not set
   */
  fun getDisplayMode(): BookmarkDisplayMode

  /**
   * Save bookmark display mode preference.
   *
   * @param mode Display mode to save
   */
  fun setDisplayMode(mode: BookmarkDisplayMode)

  /**
   * Retrieve theme mode preference.
   *
   * @return Stored theme mode, defaults to System if not set
   */
  fun getThemeMode(): ThemeMode

  /**
   * Save theme mode preference.
   *
   * @param mode Theme mode to save
   */
  fun setThemeMode(mode: ThemeMode)

  /**
   * Retrieve language mode preference.
   *
   * @return Stored language mode, defaults to System if not set
   */
  fun getLanguageMode(): LanguageMode

  /**
   * Save language mode preference.
   *
   * @param mode Language mode to save
   */
  fun setLanguageMode(mode: LanguageMode)

  /**
   * Retrieve user-configured bootstrap relay URLs.
   *
   * @return Set of relay URLs, or null if user has never configured relays
   */
  fun getBootstrapRelays(): Set<String>?

  /**
   * Save user-configured bootstrap relay URLs.
   *
   * @param relays Set of relay URLs to save
   */
  fun setBootstrapRelays(relays: Set<String>)
}
