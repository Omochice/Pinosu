package io.github.omochice.pinosu.feature.settings.presentation.viewmodel

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.AppLocale

/**
 * UI state for Settings screen.
 *
 * @property displayMode Current bookmark display mode preference
 * @property locale Current application locale preference
 */
data class SettingsUiState(
    val displayMode: BookmarkDisplayMode = BookmarkDisplayMode.List,
    val locale: AppLocale = AppLocale.System,
)
