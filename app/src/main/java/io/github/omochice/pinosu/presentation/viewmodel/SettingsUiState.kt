package io.github.omochice.pinosu.presentation.viewmodel

import io.github.omochice.pinosu.domain.model.ThemeMode

/**
 * Settings screen UI state
 *
 * @property themeMode Current selected theme mode
 */
data class SettingsUiState(val themeMode: ThemeMode = ThemeMode.System)
