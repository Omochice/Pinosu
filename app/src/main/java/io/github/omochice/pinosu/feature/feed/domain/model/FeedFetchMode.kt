package io.github.omochice.pinosu.feature.feed.domain.model

/** How much a copy of a feed kept from an earlier download is trusted */
enum class FeedFetchMode {
  /** A kept copy is used for as long as its server said it stays current */
  Normal,

  /** The server is asked whether a kept copy is still current, however recently it was fetched */
  Revalidate,
}
