package io.github.omochice.pinosu.data.repository

import io.github.omochice.pinosu.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

/** Repository for app settings */
interface SettingsRepository {

  /** Current theme mode as reactive stream */
  val themeMode: StateFlow<ThemeMode>

  /** Get current theme mode */
  fun getThemeMode(): ThemeMode

  /** Set theme mode */
  fun setThemeMode(mode: ThemeMode)
}
