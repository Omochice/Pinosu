package io.github.omochice.pinosu.feature.settings.presentation.viewmodel

import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode

/**
 * UI state for Settings screen.
 *
 * @property displayMode Current bookmark display mode preference
 */
data class SettingsUiState(
    val displayMode: BookmarkDisplayMode = BookmarkDisplayMode.List,
)
