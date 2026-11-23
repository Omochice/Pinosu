package io.github.omochice.pinosu.feature.feed.domain.model

/** Outcome of loading the user's feed subscription list from relays */
sealed interface FeedSubscriptionLoadResult {

  /**
   * A relay returned the list
   *
   * @property list The newest list returned by any relay
   */
  data class Found(val list: FeedSubscriptionList) : FeedSubscriptionLoadResult

  /** A relay confirmed that it holds no list, so the user has not published one yet */
  data object Absent : FeedSubscriptionLoadResult

  /**
   * No relay could tell whether the list exists, so a new list must not be published over it
   *
   * @property reason Why no relay answered
   */
  data class Unavailable(val reason: Reason) : FeedSubscriptionLoadResult

  /** Why the list could not be loaded */
  enum class Reason {
    /** The user has no relays of their own to query */
    NO_USER_RELAYS,

    /** Every relay refused to serve the list to this client */
    ACCESS_DENIED,

    /** No relay answered, because of failures, timeouts, or other refusals */
    UNREACHABLE,
  }
}
