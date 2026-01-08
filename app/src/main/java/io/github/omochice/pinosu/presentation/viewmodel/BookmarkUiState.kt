package io.github.omochice.pinosu.presentation.viewmodel

import io.github.omochice.pinosu.domain.model.BookmarkItem

/**
 * UI state for bookmark list screen
 *
 * @property isLoading Whether bookmark data is currently being loaded
 * @property bookmarks List of bookmark items to display
 * @property error Error message if loading failed
 */
data class BookmarkUiState(
    val isLoading: Boolean = false,
    val bookmarks: List<BookmarkItem> = emptyList(),
    val error: String? = null,
)
