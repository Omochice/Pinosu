package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.feature.feed.domain.model.FeedTimeline

/** Use case that builds the logged-in user's timeline from every feed they subscribe to */
interface LoadFeedTimelineUseCase {

  /**
   * Load the subscription list and fetch every subscribed feed
   *
   * A feed that cannot be fetched is reported in [FeedTimeline.failedFeeds] instead of failing the
   * whole timeline. Each item and failure carries the category its feed is in on the subscription
   * list, whatever URL the fetched feed document names.
   *
   * @return The timeline, empty when the user has no list yet, or a failure with
   *   [io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException]
   *   when the list cannot be loaded, or with [IllegalStateException] when the user is not logged
   *   in
   */
  suspend operator fun invoke(): Result<FeedTimeline>
}
