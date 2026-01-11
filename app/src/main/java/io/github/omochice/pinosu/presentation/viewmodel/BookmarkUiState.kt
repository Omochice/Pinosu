package io.github.omochice.pinosu.presentation.viewmodel

import io.github.omochice.pinosu.domain.model.BookmarkItem

/**
 * UI state for bookmark list screen
 *
 * @property isLoading Whether bookmark data is currently being loaded
 * @property allBookmarks Complete list of all bookmark items from relay (shared data pool)
 * @property bookmarks Filtered list of bookmark items to display based on selected tab
 * @property error Error message if loading failed
 * @property selectedBookmarkForUrlDialog Bookmark item for which URL selection dialog is shown
 * @property urlOpenError Error message when URL opening fails
 * @property selectedTab Currently selected filter tab (Local or Global)
 * @property userHexPubkey Hex-encoded pubkey of logged-in user for local filtering
 */
data class BookmarkUiState(
    val isLoading: Boolean = false,
    val allBookmarks: List<BookmarkItem> = emptyList(),
    val bookmarks: List<BookmarkItem> = emptyList(),
    val error: String? = null,
    val selectedBookmarkForUrlDialog: BookmarkItem? = null,
    val urlOpenError: String? = null,
    val selectedTab: BookmarkFilterMode = BookmarkFilterMode.Local,
    val userHexPubkey: String? = null,
)
