package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.feature.feed.domain.model.FeedFetchFailure
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem

/**
 * Loaded timeline shown as a vertical list of cards
 *
 * @param items Items to show, newest first
 * @param failedFeeds Feeds that could not be fetched, listed in a notice above the items
 * @param articleImages Image of each article link looked up so far, null for an article without one
 * @param callbacks Callbacks for the items' interactions and their article image lookup
 */
@Composable
internal fun FeedList(
    items: List<FeedItem>,
    failedFeeds: List<FeedFetchFailure>,
    articleImages: Map<String, String?>,
    callbacks: FeedItemCallbacks,
) {
  LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (failedFeeds.isNotEmpty()) {
          item(key = "failedFeeds") { FailedFeedsNotice(failedFeeds = failedFeeds) }
        }
        items(items, key = ::feedItemKey) { item ->
          FeedListItem(item = item, articleImages = articleImages, callbacks = callbacks) {
              imageUrl,
              onBookmark ->
            FeedItemCard(
                item = item,
                imageUrl = imageUrl,
                onOpen = callbacks.onOpen,
                onBookmark = onBookmark)
          }
        }
      }
}

/** Lazy list key of [item]; its id is unique only within its own feed */
private fun feedItemKey(item: FeedItem): String = "${item.feedUrl}\n${item.id}"
