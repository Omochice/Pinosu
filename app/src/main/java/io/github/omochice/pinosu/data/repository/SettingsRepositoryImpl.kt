package io.github.omochice.pinosu.data.repository

import io.github.omochice.pinosu.data.local.LocalSettingsDataSource
import io.github.omochice.pinosu.domain.model.ThemeMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementation of settings repository
 *
 * Manages app settings with reactive state for theme mode.
 */
@Singleton
class SettingsRepositoryImpl
@Inject
constructor(private val localSettingsDataSource: LocalSettingsDataSource) : SettingsRepository {
  private val _themeMode = MutableStateFlow(localSettingsDataSource.getThemeMode())
  override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

  override fun getThemeMode(): ThemeMode = _themeMode.value

  override fun setThemeMode(mode: ThemeMode) {
    localSettingsDataSource.setThemeMode(mode)
    _themeMode.value = mode
  }
}
