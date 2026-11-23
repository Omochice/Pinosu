package io.github.omochice.pinosu.feature.feed.domain.repository

import io.github.omochice.pinosu.feature.feed.domain.model.FeedResolution

/** Repository that finds the feed a user means when adding a subscription */
interface FeedDiscoveryRepository {

  /**
   * Find the feed at, or advertised by, a URL entered by the user
   *
   * Input without a scheme, and `http://` input, are fetched over HTTPS.
   *
   * @param input A feed URL or a web site URL, with or without a scheme
   * @return The resolution, or a failure with
   *   [io.github.omochice.pinosu.feature.feed.domain.model.error.NoFeedFoundException] when the
   *   page advertises no feed, with
   *   [io.github.omochice.pinosu.feature.feed.domain.model.error.HttpFeedNotSupportedException]
   *   when `http://` input has no reachable HTTPS counterpart, or with another exception when the
   *   download fails
   */
  suspend fun resolve(input: String): Result<FeedResolution>
}
