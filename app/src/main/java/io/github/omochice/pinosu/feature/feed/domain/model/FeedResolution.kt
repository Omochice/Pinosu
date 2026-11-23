package io.github.omochice.pinosu.feature.feed.domain.model

/** What a URL entered by the user turned out to point at */
sealed interface FeedResolution {

  /**
   * The URL led to a single feed, either directly or through the only feed its page advertises
   *
   * @property feedUrl URL to subscribe to
   * @property feed The feed as currently published
   */
  data class Direct(val feedUrl: String, val feed: Feed) : FeedResolution

  /**
   * The URL is a web page that advertises several feeds, so the user has to choose one
   *
   * @property candidates Advertised feeds in page order, at least two
   */
  data class Candidates(val candidates: List<FeedCandidate>) : FeedResolution
}
