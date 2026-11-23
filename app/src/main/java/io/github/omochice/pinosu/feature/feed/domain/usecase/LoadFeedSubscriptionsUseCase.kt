package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscription

/** Use case that loads the feeds the logged-in user subscribes to */
interface LoadFeedSubscriptionsUseCase {

  /**
   * Load the subscription list from the user's relays
   *
   * @return Subscribed feeds in their stored order, empty when the user has no list yet, or a
   *   failure with
   *   [io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException]
   *   when the list cannot be loaded, or with [IllegalStateException] when the user is not logged
   *   in
   */
  suspend operator fun invoke(): Result<List<FeedSubscription>>
}
