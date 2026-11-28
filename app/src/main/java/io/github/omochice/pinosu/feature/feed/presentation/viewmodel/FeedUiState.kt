package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import io.github.omochice.pinosu.feature.feed.domain.model.FeedFetchFailure
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem

/**
 * UI state of the feed timeline screen
 *
 * @property content What the timeline shows under the tab showing every feed; [contentIn] narrows
 *   it to the tab of a category
 * @property isRefreshing Whether a pull-to-refresh reload is running over [content]
 * @property categories Names of the category tabs that follow the tab showing every feed, in the
 *   order to show them
 * @property selectedCategory Category whose tab is selected, or null for the tab showing every feed
 */
data class FeedUiState(
    val content: Content = Content.Loading,
    val isRefreshing: Boolean = false,
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
) {

  /**
   * What the tab of a category shows of the timeline
   *
   * @param category Category of the tab, or null for the tab showing every feed
   */
  fun contentIn(category: String?): Content =
      if (category == null || content !is Content.Loaded) {
        content
      } else {
        Content.Loaded(
            items = content.items.filter { it.feedCategory == category },
            failedFeeds = content.failedFeeds.filter { it.feedCategory == category })
      }

  /** What the timeline shows */
  sealed interface Content {

    /** The first load has not finished yet */
    data object Loading : Content

    /** The user subscribes to no feed that produced an item or a failure */
    data object Empty : Content

    /**
     * The timeline loaded
     *
     * @property items Items newest first
     * @property failedFeeds Subscribed feeds that could not be fetched
     */
    data class Loaded(val items: List<FeedItem>, val failedFeeds: List<FeedFetchFailure>) : Content

    /**
     * The timeline could not be built at all
     *
     * @property reason Why loading failed
     */
    data class Error(val reason: FeedLoadError) : Content
  }
}
