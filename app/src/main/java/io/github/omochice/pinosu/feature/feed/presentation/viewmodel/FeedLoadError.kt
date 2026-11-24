package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException

/** Why the feed subscription list, and so anything built from it, could not be loaded */
enum class FeedLoadError {
  /** The user has no relays of their own to load the subscription list from */
  NoUserRelays,

  /** Every relay refused to serve the subscription list */
  AccessDenied,

  /** No relay answered for the subscription list */
  Unreachable,

  /** Any other failure */
  Unknown,
}

/**
 * Classify a failure to load the subscription list
 *
 * @return [FeedLoadError.Unknown] for anything but a list that no relay served
 */
internal fun feedLoadErrorOf(error: Throwable): FeedLoadError =
    if (error is FeedSubscriptionListUnavailableException) {
      feedLoadErrorOf(error.reason)
    } else {
      FeedLoadError.Unknown
    }

/** Classify why no relay served the subscription list */
internal fun feedLoadErrorOf(reason: FeedSubscriptionLoadResult.Reason): FeedLoadError =
    when (reason) {
      FeedSubscriptionLoadResult.Reason.NO_USER_RELAYS -> FeedLoadError.NoUserRelays
      FeedSubscriptionLoadResult.Reason.ACCESS_DENIED -> FeedLoadError.AccessDenied
      FeedSubscriptionLoadResult.Reason.UNREACHABLE -> FeedLoadError.Unreachable
    }
