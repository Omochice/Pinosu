package io.github.omochice.pinosu.feature.settings.presentation.viewmodel

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode

/**
 * UI state for Settings screen.
 *
 * @property displayMode Current bookmark display mode preference
 * @property themeMode Current theme mode preference
 */
data class SettingsUiState(
    val displayMode: BookmarkDisplayMode = BookmarkDisplayMode.List,
    val themeMode: ThemeMode = ThemeMode.System,
)
