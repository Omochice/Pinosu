package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.core.relay.PublishResult

/** Use case that publishes a signed subscription list event */
interface PublishFeedSubscriptionListUseCase {

  /**
   * Publish a signed subscription list event to the user's write relays
   *
   * @param signedEventJson Signed event as JSON string
   * @return Which relays accepted and rejected the event, or a failure when publishing fails
   */
  suspend operator fun invoke(signedEventJson: String): Result<PublishResult>
}
