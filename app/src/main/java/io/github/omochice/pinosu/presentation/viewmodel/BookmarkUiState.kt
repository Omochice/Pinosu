package io.github.omochice.pinosu.presentation.viewmodel

import io.github.omochice.pinosu.domain.model.BookmarkItem

/**
 * UI state for bookmark list screen
 *
 * @property isLoading Whether bookmark data is currently being loaded
 * @property bookmarks List of bookmark items to display
 * @property error Error message if loading failed
 * @property selectedBookmarkForUrlDialog Bookmark item for which URL selection dialog is shown
 * @property urlOpenError Error message when URL opening fails
 */
data class BookmarkUiState(
    val isLoading: Boolean = false,
    val bookmarks: List<BookmarkItem> = emptyList(),
    val error: String? = null,
    val selectedBookmarkForUrlDialog: BookmarkItem? = null,
    val urlOpenError: String? = null,
)
