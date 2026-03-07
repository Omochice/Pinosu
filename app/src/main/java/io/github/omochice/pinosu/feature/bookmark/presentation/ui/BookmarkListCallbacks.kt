package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem

/**
 * Grouped callbacks for bookmark list user interactions
 *
 * @property onRefresh Callback when pull-to-refresh is triggered
 * @property onClick Callback when a bookmark card is tapped
 * @property onLongPress Callback when a bookmark card is long-pressed
 * @property onLoadMore Callback when user scrolls near the end of the list to load older bookmarks
 */
data class BookmarkListCallbacks(
    val onRefresh: () -> Unit,
    val onClick: (BookmarkItem) -> Unit,
    val onLongPress: (BookmarkItem) -> Unit,
    val onLoadMore: () -> Unit,
)
