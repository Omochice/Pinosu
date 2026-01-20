package io.github.omochice.pinosu.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local data source for application settings.
 *
 * Uses SharedPreferences to store non-sensitive user preferences.
 */
@Singleton
class LocalSettingsDataSource
@Inject
constructor(@ApplicationContext private val context: Context) {
  private val sharedPreferences: SharedPreferences by lazy {
    testSharedPreferences ?: context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  private var testSharedPreferences: SharedPreferences? = null

  /** For testing only - sets SharedPreferences before first access */
  internal fun setTestSharedPreferences(prefs: SharedPreferences) {
    testSharedPreferences = prefs
  }

  companion object {
    private const val PREFS_NAME = "pinosu_settings"
    internal const val KEY_DISPLAY_MODE = "bookmark_display_mode"
  }

  /**
   * Retrieve bookmark display mode preference.
   *
   * @return Stored display mode, defaults to List if not set or invalid
   */
  fun getDisplayMode(): BookmarkDisplayMode {
    val value = sharedPreferences.getString(KEY_DISPLAY_MODE, null)
    return try {
      value?.let { BookmarkDisplayMode.valueOf(it) } ?: BookmarkDisplayMode.List
    } catch (e: IllegalArgumentException) {
      BookmarkDisplayMode.List
    }
  }

  /**
   * Save bookmark display mode preference.
   *
   * @param mode Display mode to save
   */
  fun setDisplayMode(mode: BookmarkDisplayMode) {
    sharedPreferences.edit().putString(KEY_DISPLAY_MODE, mode.name).apply()
  }
}
