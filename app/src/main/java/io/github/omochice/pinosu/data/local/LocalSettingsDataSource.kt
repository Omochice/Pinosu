package io.github.omochice.pinosu.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Local data source for application settings.
 *
 * Uses SharedPreferences to store non-sensitive user preferences. Provides observable StateFlow for
 * reactive updates.
 */
@Singleton
class LocalSettingsDataSource
@Inject
constructor(@param:ApplicationContext private val context: Context) {
  private val sharedPreferences: SharedPreferences by lazy {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  private val _displayModeFlow = MutableStateFlow(getDisplayMode())

  /** Observable StateFlow of display mode preference */
  val displayModeFlow: StateFlow<BookmarkDisplayMode> = _displayModeFlow.asStateFlow()

  /**
   * Retrieve bookmark display mode preference.
   *
   * @return Stored display mode, defaults to List if not set or invalid
   */
  fun getDisplayMode(): BookmarkDisplayMode {
    val value = sharedPreferences.getString(KEY_DISPLAY_MODE, null)
    return try {
      value?.let { BookmarkDisplayMode.valueOf(it) } ?: BookmarkDisplayMode.List
    } catch (_: IllegalArgumentException) {
      BookmarkDisplayMode.List
    }
  }

  /**
   * Save bookmark display mode preference and emit to observers.
   *
   * @param mode Display mode to save
   */
  fun setDisplayMode(mode: BookmarkDisplayMode) {
    sharedPreferences.edit().putString(KEY_DISPLAY_MODE, mode.name).apply()
    _displayModeFlow.value = mode
  }

  companion object {
    private const val PREFS_NAME = "pinosu_settings"
    internal const val KEY_DISPLAY_MODE = "bookmark_display_mode"
  }
}
