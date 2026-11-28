package io.github.omochice.pinosu.feature.feed.domain.usecase

import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionList
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.github.omochice.pinosu.feature.feed.domain.model.error.FeedSubscriptionListUnavailableException

/**
 * The loaded list as a result
 *
 * @return The list, null when the user has not published one, or a failure with
 *   [FeedSubscriptionListUnavailableException] when no relay could tell
 */
internal fun FeedSubscriptionLoadResult.toListResult(): Result<FeedSubscriptionList?> =
    when (this) {
      is FeedSubscriptionLoadResult.Found -> Result.success(list)
      FeedSubscriptionLoadResult.Absent -> Result.success(null)
      is FeedSubscriptionLoadResult.Unavailable ->
          Result.failure(FeedSubscriptionListUnavailableException(reason))
    }
