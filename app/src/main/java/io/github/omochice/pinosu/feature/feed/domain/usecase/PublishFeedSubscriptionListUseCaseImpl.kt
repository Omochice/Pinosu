package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedSubscriptionRepository
import javax.inject.Inject

/**
 * Implementation of [PublishFeedSubscriptionListUseCase]
 *
 * @param subscriptionRepository Repository for the subscription list
 */
class PublishFeedSubscriptionListUseCaseImpl
@Inject
constructor(private val subscriptionRepository: FeedSubscriptionRepository) :
    PublishFeedSubscriptionListUseCase {

  override suspend fun invoke(signedEventJson: String): Result<PublishResult> =
      subscriptionRepository.publishSubscriptionList(signedEventJson)
}
