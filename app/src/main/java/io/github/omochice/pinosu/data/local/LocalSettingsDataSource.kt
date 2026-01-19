package io.github.omochice.pinosu.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.omochice.pinosu.domain.model.ThemeMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local settings data source
 *
 * Uses SharedPreferences to store and retrieve non-sensitive app settings such as theme mode.
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
    internal const val KEY_THEME_MODE = "theme_mode"
  }

  /**
   * Get current theme mode
   *
   * @return Saved theme mode, defaults to System if not set or invalid
   */
  fun getThemeMode(): ThemeMode {
    val ordinal = sharedPreferences.getInt(KEY_THEME_MODE, ThemeMode.System.ordinal)
    return ThemeMode.entries.getOrElse(ordinal) { ThemeMode.System }
  }

  /**
   * Save theme mode
   *
   * @param mode Theme mode to save
   */
  fun setThemeMode(mode: ThemeMode) {
    sharedPreferences.edit().putInt(KEY_THEME_MODE, mode.ordinal).apply()
  }
}
