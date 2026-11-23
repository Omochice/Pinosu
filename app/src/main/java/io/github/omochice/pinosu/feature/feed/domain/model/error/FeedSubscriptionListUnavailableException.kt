package io.github.omochice.pinosu.feature.feed.domain.model.error

import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult

/**
 * The user's subscription list could not be loaded from any relay
 *
 * @property reason Why no relay answered
 */
class FeedSubscriptionListUnavailableException(val reason: FeedSubscriptionLoadResult.Reason) :
    Exception("Feed subscription list is unavailable: $reason")
