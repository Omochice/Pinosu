package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.core.result.andThen
import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.feed.domain.model.FeedResolution
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscription
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionChange
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionList
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException
import io.github.omochice.pinosu.feature.feed.domain.model.error.ReadOnlyLoginException
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedDiscoveryRepository
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedSubscriptionRepository
import javax.inject.Inject

/** Implementation of [PrepareFeedSubscriptionChangeUseCase] */
class PrepareFeedSubscriptionChangeUseCaseImpl
@Inject
constructor(
    private val subscriptionRepository: FeedSubscriptionRepository,
    private val discoveryRepository: FeedDiscoveryRepository,
    private val getLoginStateUseCase: GetLoginStateUseCase,
    private val authRepository: AuthRepository,
) : PrepareFeedSubscriptionChangeUseCase {

  override suspend fun add(input: String): Result<FeedSubscriptionChange> =
      writerPubkey().andThen { hexPubkey -> addResolved(hexPubkey, input) }

  override suspend fun remove(feedUrl: String): Result<FeedSubscriptionChange> =
      writerPubkey().andThen { hexPubkey ->
        rewrite(hexPubkey, { FeedSubscriptionChange.NotSubscribed(feedUrl) }) { feeds ->
          feeds.filterNot { it.url == feedUrl }
        }
      }

  override suspend fun setCategory(
      feedUrl: String,
      category: String?
  ): Result<FeedSubscriptionChange> {
    val name = FeedSubscription.normalizeCategory(category)
    return writerPubkey().andThen { hexPubkey ->
      rewrite(hexPubkey, { feeds -> categoryUnchangedOrMissing(feeds, feedUrl) }) { feeds ->
        feeds.map { if (it.url == feedUrl) it.copy(category = name) else it }
      }
    }
  }

  private suspend fun writerPubkey(): Result<String> {
    val user =
        getLoginStateUseCase() ?: return Result.failure(IllegalStateException("User not logged in"))
    if (authRepository.getLoginMode().isReadOnly) return Result.failure(ReadOnlyLoginException())
    return user.pubkey.hex?.let { Result.success(it) }
        ?: Result.failure(IllegalArgumentException("Invalid npub format"))
  }

  private suspend fun addResolved(
      hexPubkey: String,
      input: String
  ): Result<FeedSubscriptionChange> =
      discoveryRepository.resolve(input).andThen { resolution ->
        when (resolution) {
          is FeedResolution.Candidates ->
              Result.success(FeedSubscriptionChange.ChooseCandidate(resolution.candidates))
          is FeedResolution.Direct -> addFeed(hexPubkey, resolution.feedUrl)
        }
      }

  private suspend fun addFeed(hexPubkey: String, feedUrl: String): Result<FeedSubscriptionChange> =
      rewrite(hexPubkey, { FeedSubscriptionChange.AlreadySubscribed(feedUrl) }) { feeds ->
        (feeds + FeedSubscription(feedUrl)).distinctBy { it.url }
      }

  private suspend fun rewrite(
      hexPubkey: String,
      unchanged: (List<FeedSubscription>) -> FeedSubscriptionChange,
      edit: (List<FeedSubscription>) -> List<FeedSubscription>,
  ): Result<FeedSubscriptionChange> =
      when (val loaded = subscriptionRepository.loadSubscriptionList(hexPubkey)) {
        is FeedSubscriptionLoadResult.Found ->
            Result.success(prepare(hexPubkey, loaded.list, edit, unchanged))
        FeedSubscriptionLoadResult.Absent ->
            Result.success(prepare(hexPubkey, null, edit, unchanged))
        is FeedSubscriptionLoadResult.Unavailable ->
            Result.failure(FeedSubscriptionListUnavailableException(loaded.reason))
      }

  private fun categoryUnchangedOrMissing(
      feeds: List<FeedSubscription>,
      feedUrl: String,
  ): FeedSubscriptionChange =
      if (feeds.any { it.url == feedUrl }) {
        FeedSubscriptionChange.CategoryUnchanged(feedUrl)
      } else {
        FeedSubscriptionChange.NotSubscribed(feedUrl)
      }

  private fun prepare(
      hexPubkey: String,
      previous: FeedSubscriptionList?,
      edit: (List<FeedSubscription>) -> List<FeedSubscription>,
      unchanged: (List<FeedSubscription>) -> FeedSubscriptionChange,
  ): FeedSubscriptionChange {
    val current = previous?.feeds.orEmpty()
    val feeds = edit(current)
    return if (feeds == current) {
      unchanged(current)
    } else {
      FeedSubscriptionChange.ReadyToSign(
          event = subscriptionRepository.createSubscriptionListEvent(hexPubkey, feeds, previous),
          feeds = feeds,
          isFirstList = previous == null,
      )
    }
  }
}
