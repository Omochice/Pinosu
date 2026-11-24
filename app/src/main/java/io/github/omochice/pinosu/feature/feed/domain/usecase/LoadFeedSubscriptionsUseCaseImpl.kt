package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscription
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedSubscriptionRepository
import javax.inject.Inject

/** Implementation of [LoadFeedSubscriptionsUseCase] */
class LoadFeedSubscriptionsUseCaseImpl
@Inject
constructor(
    private val subscriptionRepository: FeedSubscriptionRepository,
    private val getLoginStateUseCase: GetLoginStateUseCase,
) : LoadFeedSubscriptionsUseCase {

  override suspend fun invoke(): Result<List<FeedSubscription>> {
    val user =
        getLoginStateUseCase() ?: return Result.failure(IllegalStateException("User not logged in"))
    val hexPubkey =
        user.pubkey.hex ?: return Result.failure(IllegalArgumentException("Invalid npub format"))

    return subscriptionRepository.loadSubscriptionList(hexPubkey).toListResult().map {
      it?.feeds.orEmpty()
    }
  }
}
