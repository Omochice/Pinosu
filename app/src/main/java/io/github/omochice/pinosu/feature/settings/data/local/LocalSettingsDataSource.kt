package io.github.omochice.pinosu.feature.settings.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
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

  private val _themeModeFlow = MutableStateFlow(getThemeMode())

  /** Observable StateFlow of theme mode preference */
  val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

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

  /**
   * Retrieve theme mode preference.
   *
   * @return Stored theme mode, defaults to System if not set or invalid
   */
  fun getThemeMode(): ThemeMode {
    val value = sharedPreferences.getString(KEY_THEME_MODE, null)
    return try {
      value?.let { ThemeMode.valueOf(it) } ?: ThemeMode.System
    } catch (_: IllegalArgumentException) {
      ThemeMode.System
    }
  }

  /**
   * Save theme mode preference and emit to observers.
   *
   * @param mode Theme mode to save
   */
  fun setThemeMode(mode: ThemeMode) {
    sharedPreferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
    _themeModeFlow.value = mode
  }

  companion object {
    private const val PREFS_NAME = "pinosu_settings"
    internal const val KEY_DISPLAY_MODE = "bookmark_display_mode"
    internal const val KEY_THEME_MODE = "theme_mode"
  }
}
