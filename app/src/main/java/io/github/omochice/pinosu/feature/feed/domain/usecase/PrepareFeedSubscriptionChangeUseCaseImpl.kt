package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.feed.domain.model.FeedResolution
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionChange
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionList
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException
import io.github.omochice.pinosu.feature.feed.domain.model.error.ReadOnlyLoginException
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedDiscoveryRepository
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedSubscriptionRepository
import javax.inject.Inject

/**
 * Implementation of [PrepareFeedSubscriptionChangeUseCase]
 *
 * @param subscriptionRepository Repository for the subscription list
 * @param discoveryRepository Repository that finds the feed the user entered
 * @param getLoginStateUseCase UseCase for retrieving current login state
 * @param authRepository Repository that tells whether the user logged in read-only
 */
class PrepareFeedSubscriptionChangeUseCaseImpl
@Inject
constructor(
    private val subscriptionRepository: FeedSubscriptionRepository,
    private val discoveryRepository: FeedDiscoveryRepository,
    private val getLoginStateUseCase: GetLoginStateUseCase,
    private val authRepository: AuthRepository,
) : PrepareFeedSubscriptionChangeUseCase {

  override suspend fun add(input: String): Result<FeedSubscriptionChange> =
      writerPubkey()
          .fold(
              onSuccess = { hexPubkey -> addResolved(hexPubkey, input) },
              onFailure = { Result.failure(it) },
          )

  override suspend fun remove(feedUrl: String): Result<FeedSubscriptionChange> =
      writerPubkey()
          .fold(
              onSuccess = { hexPubkey ->
                rewrite(hexPubkey, FeedSubscriptionChange.NotSubscribed(feedUrl)) { it - feedUrl }
              },
              onFailure = { Result.failure(it) },
          )

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
      discoveryRepository
          .resolve(input)
          .fold(
              onSuccess = { resolution ->
                when (resolution) {
                  is FeedResolution.Candidates ->
                      Result.success(FeedSubscriptionChange.ChooseCandidate(resolution.candidates))
                  is FeedResolution.Direct -> addFeed(hexPubkey, resolution.feedUrl)
                }
              },
              onFailure = { Result.failure(it) },
          )

  private suspend fun addFeed(hexPubkey: String, feedUrl: String): Result<FeedSubscriptionChange> =
      rewrite(hexPubkey, FeedSubscriptionChange.AlreadySubscribed(feedUrl)) {
        (it + feedUrl).distinct()
      }

  private suspend fun rewrite(
      hexPubkey: String,
      unchanged: FeedSubscriptionChange,
      edit: (List<String>) -> List<String>,
  ): Result<FeedSubscriptionChange> =
      when (val loaded = subscriptionRepository.loadSubscriptionList(hexPubkey)) {
        is FeedSubscriptionLoadResult.Found ->
            Result.success(prepare(hexPubkey, loaded.list, edit, unchanged))
        FeedSubscriptionLoadResult.Absent ->
            Result.success(prepare(hexPubkey, null, edit, unchanged))
        is FeedSubscriptionLoadResult.Unavailable ->
            Result.failure(FeedSubscriptionListUnavailableException(loaded.reason))
      }

  private fun prepare(
      hexPubkey: String,
      previous: FeedSubscriptionList?,
      edit: (List<String>) -> List<String>,
      unchanged: FeedSubscriptionChange,
  ): FeedSubscriptionChange {
    val current = previous?.feedUrls.orEmpty()
    val feedUrls = edit(current)
    return if (feedUrls == current) {
      unchanged
    } else {
      FeedSubscriptionChange.ReadyToSign(
          event = subscriptionRepository.createSubscriptionListEvent(hexPubkey, feedUrls, previous),
          feedUrls = feedUrls,
          isFirstList = previous == null,
      )
    }
  }
}
