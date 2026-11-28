package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem

/**
 * Card of [item] in the timeline, asking for the image of its article while shown when the feed
 * gives it none
 *
 * The lookup is started from the composed item rather than for every item when the timeline loads,
 * so that only the pages of items the user scrolls to are fetched.
 *
 * @param item Item to show
 * @param articleImages Image of each article link looked up so far, null for an article without one
 * @param callbacks Callbacks for the item's interactions and its article image lookup
 */
@Composable
internal fun FeedListItem(
    item: FeedItem,
    articleImages: Map<String, String?>,
    callbacks: FeedItemCallbacks,
) {
  val articleLink = item.link.takeIf { item.imageUrl == null }
  val loadArticleImage = callbacks.onLoadArticleImage
  if (articleLink != null && loadArticleImage != null) {
    LaunchedEffect(articleLink) { loadArticleImage(articleLink) }
  }
  FeedItemCard(
      item = item,
      imageUrl = item.imageUrl ?: articleLink?.let(articleImages::get),
      onOpen = callbacks.onOpen,
      onBookmark = callbacks.onBookmark?.takeIf { item.link != null })
}
