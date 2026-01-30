package io.github.omochice.pinosu.feature.settings.data.repository

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for application settings.
 *
 * Provides abstraction layer for settings data access with reactive updates.
 */
interface SettingsRepository {
  /** Observable StateFlow of display mode preference for reactive updates */
  val displayModeFlow: StateFlow<BookmarkDisplayMode>

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
}
