package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.feature.feed.domain.model.FeedFetchFailure
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscription
import io.github.omochice.pinosu.feature.feed.domain.model.FeedTimeline
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/** Implementation of [LoadFeedTimelineUseCase] */
class LoadFeedTimelineUseCaseImpl
@Inject
constructor(
    private val loadSubscriptions: LoadFeedSubscriptionsUseCase,
    private val feedRepository: FeedRepository,
) : LoadFeedTimelineUseCase {

  override suspend fun invoke(): Result<FeedTimeline> = loadSubscriptions().map { fetchAll(it) }

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
