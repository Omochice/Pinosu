package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionChange

/**
 * Use case that builds a new subscription list event from the list currently on relays
 *
 * Every call loads the list again, so a change never overwrites a list published elsewhere in the
 * meantime.
 *
 * Each method fails with [IllegalStateException] when the user is not logged in, with
 * [io.github.omochice.pinosu.feature.feed.domain.model.error.ReadOnlyLoginException] when the user
 * logged in read-only, and with
 * [io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException]
 * when the current list cannot be loaded.
 */
interface PrepareFeedSubscriptionChangeUseCase {

  /**
   * Prepare adding the feed the user entered
   *
   * @param input A feed URL or a web site URL, with or without a scheme
   * @return The prepared change, or a failure from
   *   [io.github.omochice.pinosu.feature.feed.domain.repository.FeedDiscoveryRepository.resolve]
   *   when no feed can be found at [input]
   */
  suspend fun add(input: String): Result<FeedSubscriptionChange>

  /**
   * Prepare removing a subscribed feed
   *
   * @param feedUrl URL of the feed, as subscribed
   * @return The prepared change
   */
  suspend fun remove(feedUrl: String): Result<FeedSubscriptionChange>
}
