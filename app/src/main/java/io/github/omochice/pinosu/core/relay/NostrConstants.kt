package io.github.omochice.pinosu.core.relay

/** Shared Nostr protocol constants used across relay repositories */
object NostrConstants {
  const val KIND_BOOKMARK_LIST = 39_701
  const val KIND_COMMENT = 1111
  const val PER_RELAY_TIMEOUT_MS = 10_000L
  const val DEFAULT_RELAY_URL = "wss://yabu.me"
}
