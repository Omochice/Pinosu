package io.github.omochice.pinosu.feature.feed.presentation.ui

import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem

/**
 * Grouped callbacks for feed item interactions
 *
 * @property onOpen Callback when an item is tapped
 * @property onBookmark Callback when the bookmark action of an item with a link is tapped, or null
 *   to hide the action
 * @property onLoadArticleImage Called with the link of a shown item whose feed gives it no image;
 *   cancelled when the item leaves the screen, or null while its items are composed but not on
 *   screen
 */
data class FeedItemCallbacks(
    val onOpen: (FeedItem) -> Unit,
    val onBookmark: ((FeedItem) -> Unit)?,
    val onLoadArticleImage: (suspend (link: String) -> Unit)?,
) {

  /**
   * These callbacks for the items of a pager page, which must not look up article images while the
   * page is composed but not on screen
   *
   * @param isShown Whether the page is in the viewport
   */
  fun forPage(isShown: Boolean): FeedItemCallbacks =
      if (isShown) this else copy(onLoadArticleImage = null)
}
