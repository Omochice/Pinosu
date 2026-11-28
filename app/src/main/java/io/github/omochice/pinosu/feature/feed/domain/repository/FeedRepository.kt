package io.github.omochice.pinosu.feature.feed.domain.repository

import io.github.omochice.pinosu.feature.feed.domain.model.Feed

/** Repository for the current contents of subscribed feeds */
interface FeedRepository {

  /**
   * Fetch one feed
   *
   * An `http://` URL is fetched over HTTPS, because the app does not allow cleartext traffic.
   *
   * @param feedUrl URL of the feed, as subscribed
   * @return The feed with [Feed.url] set to [feedUrl], or a failure with
   *   [io.github.omochice.pinosu.feature.feed.domain.model.error.HttpFeedNotSupportedException]
   *   when [feedUrl] is `http://` and its HTTPS counterpart cannot be fetched, or with another
   *   exception when the download fails or the document is not a feed
   */
  suspend fun fetchFeed(feedUrl: String): Result<Feed>
}
