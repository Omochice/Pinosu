package io.github.omochice.pinosu.feature.feed.domain.usecase

/** Use case that loads the feed URLs the logged-in user subscribes to */
interface LoadFeedSubscriptionsUseCase {

  /**
   * Load the subscription list from the user's relays
   *
   * @return Subscribed feed URLs in their stored order, empty when the user has no list yet, or a
   *   failure with
   *   [io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException]
   *   when the list cannot be loaded, or with [IllegalStateException] when the user is not logged
   *   in
   */
  suspend operator fun invoke(): Result<List<String>>
}
