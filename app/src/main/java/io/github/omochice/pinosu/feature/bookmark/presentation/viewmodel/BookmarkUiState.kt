package io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem

/**
 * Loading and data state for a single bookmark tab (Local or Global)
 *
 * Each tab owns an independent query and pagination cursor so that the author-constrained Local
 * query and the unconstrained Global query never share state.
 *
 * @property items Bookmark items fetched for this tab
 * @property isLoading Whether the first page is currently being loaded
 * @property isLoadingMore Whether an additional (older) page is currently being fetched
 * @property hasMoreItems Whether the relay may still have older items to fetch
 * @property error Error message if loading failed
 */
data class BookmarkTabState(
    val items: List<BookmarkItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreItems: Boolean = true,
    val error: String? = null,
)

/**
 * UI state for bookmark list screen
 *
 * @property local State for the author-constrained Local tab
 * @property global State for the unconstrained Global tab
 * @property selectedBookmarkForUrlDialog Bookmark item for which URL selection dialog is shown
 * @property urlOpenError Error message when URL opening fails
 * @property selectedTab Currently selected filter tab (Local or Global)
 * @property displayMode Current display mode for bookmark list (List or Grid)
 */
data class BookmarkUiState(
    val local: BookmarkTabState = BookmarkTabState(),
    val global: BookmarkTabState = BookmarkTabState(),
    val selectedBookmarkForUrlDialog: BookmarkItem? = null,
    val urlOpenError: String? = null,
    val selectedTab: BookmarkFilterMode = BookmarkFilterMode.Local,
    val displayMode: BookmarkDisplayMode = BookmarkDisplayMode.List,
) {
  /** Returns the [BookmarkTabState] backing the given [mode]. */
  fun tab(mode: BookmarkFilterMode): BookmarkTabState =
      when (mode) {
        BookmarkFilterMode.Local -> local
        BookmarkFilterMode.Global -> global
      }
}
