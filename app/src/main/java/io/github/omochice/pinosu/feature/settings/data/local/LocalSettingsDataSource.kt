package io.github.omochice.pinosu.feature.settings.data.local

import android.app.LocaleManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.AppLocale
import java.util.Locale
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

  /**
   * Retrieve current application locale.
   *
   * Uses [LocaleManager] on API 33+ for direct platform integration, falls back to
   * [AppCompatDelegate] on older API levels.
   *
   * @return Current [AppLocale], defaults to [AppLocale.System] if none set
   */
  fun getLocale(): AppLocale {
    val tag =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
          if (locales.isEmpty) null else locales[0].toLanguageTag()
        } else {
          val locales = AppCompatDelegate.getApplicationLocales()
          if (locales.isEmpty) null else locales[0]?.toLanguageTag()
        }
    return AppLocale.fromTag(tag)
  }

  /**
   * Apply application locale.
   *
   * Uses [LocaleManager] on API 33+ for direct platform integration, falls back to
   * [AppCompatDelegate] on older API levels.
   *
   * @param locale [AppLocale] to apply
   */
  fun setLocale(locale: AppLocale) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val localeManager = context.getSystemService(LocaleManager::class.java)
      localeManager.applicationLocales =
          if (locale == AppLocale.System) {
            LocaleList.getEmptyLocaleList()
          } else {
            LocaleList(Locale.forLanguageTag(locale.tag))
          }
      return
    }
    val localeList =
        if (locale == AppLocale.System) {
          LocaleListCompat.getEmptyLocaleList()
        } else {
          LocaleListCompat.forLanguageTags(locale.tag)
        }
    AppCompatDelegate.setApplicationLocales(localeList)
  }

  companion object {
    private const val PREFS_NAME = "pinosu_settings"
    internal const val KEY_DISPLAY_MODE = "bookmark_display_mode"
  }
}
