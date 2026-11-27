package io.github.omochice.pinosu.feature.feed.data.repository

import io.github.omochice.pinosu.core.result.andThen
import io.github.omochice.pinosu.feature.feed.data.http.HttpDocumentFetcher
import io.github.omochice.pinosu.feature.feed.data.http.HttpsDocumentFetcher
import io.github.omochice.pinosu.feature.feed.data.parser.FeedParser
import io.github.omochice.pinosu.feature.feed.domain.model.Feed
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

/**
 * Implementation of [FeedRepository] that downloads feeds over HTTPS
 *
 * @param okHttpClient Client the feeds are downloaded with
 * @param parser Parser of the downloaded feed documents
 */
class HttpFeedRepository
@Inject
constructor(okHttpClient: OkHttpClient, private val parser: FeedParser) : FeedRepository {

  private val fetcher = HttpsDocumentFetcher(HttpDocumentFetcher(okHttpClient))

  override suspend fun fetchFeed(feedUrl: String): Result<Feed> =
      fetcher.fetch(feedUrl).andThen { document ->
        withContext(Dispatchers.Default) { parser.parseFetched(document, feedUrl) }
      }
}
