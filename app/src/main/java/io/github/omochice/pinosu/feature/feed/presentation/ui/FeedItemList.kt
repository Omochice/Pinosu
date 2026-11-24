package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.feed.domain.model.FeedFetchFailure
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedUiState

/**
 * Loaded timeline laid out as a list or a grid
 *
 * @param loaded Timeline to show
 * @param displayMode Whether to lay the items out as a list or a grid
 * @param articleImages Image of each article link looked up so far, null for an article without one
 * @param callbacks Callbacks for the items' interactions and their article image lookup
 */
@Composable
internal fun FeedItems(
    loaded: FeedUiState.Content.Loaded,
    displayMode: BookmarkDisplayMode,
    articleImages: Map<String, String?>,
    callbacks: FeedItemCallbacks,
) {
  when (displayMode) {
    BookmarkDisplayMode.List ->
        FeedList(
            items = loaded.items,
            failedFeeds = loaded.failedFeeds,
            articleImages = articleImages,
            callbacks = callbacks)
    BookmarkDisplayMode.Grid ->
        FeedGrid(
            items = loaded.items,
            failedFeeds = loaded.failedFeeds,
            articleImages = articleImages,
            callbacks = callbacks)
  }
}

/**
 * Loaded timeline shown as a vertical list of cards
 *
 * @param items Items to show, newest first
 * @param failedFeeds Feeds that could not be fetched, listed in a notice above the items
 * @param articleImages Image of each article link looked up so far, null for an article without one
 * @param callbacks Callbacks for the items' interactions and their article image lookup
 */
@Composable
private fun FeedList(
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

/**
 * Loaded timeline shown as a staggered grid of cards with their images on top
 *
 * @param items Items to show, newest first
 * @param failedFeeds Feeds that could not be fetched, listed in a notice across the top of the grid
 * @param articleImages Image of each article link looked up so far, null for an article without one
 * @param callbacks Callbacks for the items' interactions and their article image lookup
 */
@Composable
private fun FeedGrid(
    items: List<FeedItem>,
    failedFeeds: List<FeedFetchFailure>,
    articleImages: Map<String, String?>,
    callbacks: FeedItemCallbacks,
) {
  LazyVerticalStaggeredGrid(
      columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalItemSpacing = 8.dp,
      horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (failedFeeds.isNotEmpty()) {
          item(key = "failedFeeds", span = StaggeredGridItemSpan.FullLine) {
            FailedFeedsNotice(failedFeeds = failedFeeds)
          }
        }
        items(items, key = ::feedItemKey) { item ->
          FeedListItem(item = item, articleImages = articleImages, callbacks = callbacks) {
              imageUrl,
              onBookmark ->
            FeedItemGridCard(
                item = item,
                imageUrl = imageUrl,
                onOpen = callbacks.onOpen,
                onBookmark = onBookmark)
          }
        }
      }
}
