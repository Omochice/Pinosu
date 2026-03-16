package io.github.omochice.pinosu.feature.settings.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.omochice.pinosu.core.nip.nip65.Nip65RelayListFetcherImpl
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.LanguageMode
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

  private val _languageModeFlow = MutableStateFlow(getLanguageMode())

  /** Observable StateFlow of language mode preference */
  val languageModeFlow: StateFlow<LanguageMode> = _languageModeFlow.asStateFlow()

  private val _bootstrapRelaysFlow =
      MutableStateFlow(
          getBootstrapRelays() ?: Nip65RelayListFetcherImpl.DEFAULT_BOOTSTRAP_RELAY_URLS)

  /**
   * Observable StateFlow of bootstrap relay URLs (defaults are used when user has not configured)
   */
  val bootstrapRelaysFlow: StateFlow<Set<String>> = _bootstrapRelaysFlow.asStateFlow()

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
    sharedPreferences.edit { putString(KEY_DISPLAY_MODE, mode.name) }
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
    sharedPreferences.edit { putString(KEY_THEME_MODE, mode.name) }
    _themeModeFlow.value = mode
  }

  /**
   * Retrieve language mode preference.
   *
   * @return Stored language mode, defaults to System if not set or invalid
   */
  fun getLanguageMode(): LanguageMode {
    val value = sharedPreferences.getString(KEY_LANGUAGE_MODE, null)
    return try {
      value?.let { LanguageMode.valueOf(it) } ?: LanguageMode.System
    } catch (_: IllegalArgumentException) {
      LanguageMode.System
    }
  }

  /**
   * Save language mode preference and emit to observers.
   *
   * @param mode Language mode to save
   */
  fun setLanguageMode(mode: LanguageMode) {
    sharedPreferences.edit { putString(KEY_LANGUAGE_MODE, mode.name) }
    _languageModeFlow.value = mode
  }

  /**
   * Retrieve user-configured bootstrap relay URLs.
   *
   * @return Set of relay URLs, or null if user has never configured relays
   */
  fun getBootstrapRelays(): Set<String>? =
      sharedPreferences.getStringSet(KEY_BOOTSTRAP_RELAYS, null)?.toSet()

  /**
   * Save user-configured bootstrap relay URLs and emit to observers.
   *
   * @param relays Set of relay URLs to save
   */
  fun setBootstrapRelays(relays: Set<String>) {
    sharedPreferences.edit { putStringSet(KEY_BOOTSTRAP_RELAYS, relays.toSet()) }
    _bootstrapRelaysFlow.value = relays
  }

  companion object {
    internal const val PREFS_NAME = "pinosu_settings"
    internal const val KEY_DISPLAY_MODE = "bookmark_display_mode"
    internal const val KEY_THEME_MODE = "theme_mode"
    internal const val KEY_LANGUAGE_MODE = "language_mode"
    internal const val KEY_BOOTSTRAP_RELAYS = "bootstrap_relays"
  }
}
