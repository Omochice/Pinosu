package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import io.github.omochice.pinosu.feature.feed.domain.model.FeedFetchFailure
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem

/**
 * UI state of the feed timeline screen
 *
 * @property content What the timeline currently shows
 * @property isRefreshing Whether a pull-to-refresh reload is running over [content]
 */
data class FeedUiState(
    val content: Content = Content.Loading,
    val isRefreshing: Boolean = false,
) {

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
