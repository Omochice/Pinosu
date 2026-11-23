package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscription
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedSubscriptionRepository
import javax.inject.Inject

/**
 * Implementation of [LoadFeedSubscriptionsUseCase]
 *
 * @param subscriptionRepository Repository for the subscription list
 * @param getLoginStateUseCase UseCase for retrieving current login state
 */
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

    return when (val loaded = subscriptionRepository.loadSubscriptionList(hexPubkey)) {
      is FeedSubscriptionLoadResult.Found -> Result.success(loaded.list.feeds)
      FeedSubscriptionLoadResult.Absent -> Result.success(emptyList())
      is FeedSubscriptionLoadResult.Unavailable ->
          Result.failure(FeedSubscriptionListUnavailableException(loaded.reason))
    }
  }
}
