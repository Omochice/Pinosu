package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.feed.domain.model.FeedFetchFailure
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscription
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.model.FeedTimeline
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedRepository
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedSubscriptionRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Implementation of [LoadFeedTimelineUseCase]
 *
 * @param subscriptionRepository Repository for the subscription list
 * @param feedRepository Repository that fetches each feed
 * @param getLoginStateUseCase UseCase for retrieving current login state
 */
class LoadFeedTimelineUseCaseImpl
@Inject
constructor(
    private val subscriptionRepository: FeedSubscriptionRepository,
    private val feedRepository: FeedRepository,
    private val getLoginStateUseCase: GetLoginStateUseCase,
) : LoadFeedTimelineUseCase {

  override suspend fun invoke(): Result<FeedTimeline> {
    val user =
        getLoginStateUseCase() ?: return Result.failure(IllegalStateException("User not logged in"))
    val hexPubkey =
        user.pubkey.hex ?: return Result.failure(IllegalArgumentException("Invalid npub format"))

    return when (val loaded = subscriptionRepository.loadSubscriptionList(hexPubkey)) {
      is FeedSubscriptionLoadResult.Found -> Result.success(fetchAll(loaded.list.feeds))
      FeedSubscriptionLoadResult.Absent -> Result.success(FeedTimeline(emptyList(), emptyList()))
      is FeedSubscriptionLoadResult.Unavailable ->
          Result.failure(FeedSubscriptionListUnavailableException(loaded.reason))
    }
  }

  private suspend fun fetchAll(feeds: List<FeedSubscription>): FeedTimeline {
    val permits = Semaphore(MAX_CONCURRENT_FETCHES)
    val results = coroutineScope {
      feeds
          .map { feed -> async { permits.withPermit { feedRepository.fetchFeed(feed.url) } } }
          .awaitAll()
    }
    val failures =
        feeds.zip(results).mapNotNull { (feed, result) ->
          result.exceptionOrNull()?.let { FeedFetchFailure(feed.url, it, feed.category) }
        }
    val itemsPerFeed =
        feeds.zip(results).mapNotNull { (feed, result) ->
          result.getOrNull()?.items?.map { it.copy(feedCategory = feed.category) }
        }
    return FeedTimeline(
        items = mergeNewestFirst(itemsPerFeed),
        failedFeeds = failures,
        categories = feeds.mapNotNull { it.category }.distinct())
  }

  companion object {
    /** Maximum number of feeds fetched at the same time */
    const val MAX_CONCURRENT_FETCHES = 4
  }
}
