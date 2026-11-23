package io.github.omochice.pinosu.feature.feed.presentation.ui

import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem

/**
 * Grouped callbacks for feed item interactions
 *
 * @property onOpen Callback when an item is tapped
 * @property onBookmark Callback when the bookmark action of an item with a link is tapped, or null
 *   to hide the action
 */
data class FeedItemCallbacks(
    val onOpen: (FeedItem) -> Unit,
    val onBookmark: ((FeedItem) -> Unit)?,
)
