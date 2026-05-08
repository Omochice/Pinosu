package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem

/**
 * Grouped callbacks for bookmark list user interactions
 *
 * @property onRefresh Callback when pull-to-refresh is triggered
 * @property onClick Callback when a bookmark card is tapped
 * @property onLongPress Callback invoked when "Copy raw JSON" is selected from the long-press menu
 * @property onCopyNostrLink Callback invoked when "Copy nostr link" is selected from the long-press
 *   menu
 * @property onLoadMore Callback when user scrolls near the end of the list to load older bookmarks
 */
data class BookmarkListCallbacks(
    val onRefresh: () -> Unit,
    val onClick: (BookmarkItem) -> Unit,
    val onLongPress: (BookmarkItem) -> Unit,
    val onCopyNostrLink: (BookmarkItem) -> Unit,
    val onLoadMore: () -> Unit,
)
