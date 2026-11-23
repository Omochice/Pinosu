package io.github.omochice.pinosu.feature.feed.presentation.viewmodel

/** Why the feed timeline could not be loaded */
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
