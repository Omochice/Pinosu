package io.github.omochice.pinosu.feature.feed.data.repository

import io.github.omochice.pinosu.core.result.andThen
import io.github.omochice.pinosu.feature.feed.data.http.HttpsDocumentFetcher
import io.github.omochice.pinosu.feature.feed.data.parser.FeedParser
import io.github.omochice.pinosu.feature.feed.domain.model.Feed
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Implementation of [FeedRepository] that downloads feeds over HTTPS */
class HttpFeedRepository
@Inject
constructor(private val fetcher: HttpsDocumentFetcher, private val parser: FeedParser) :
    FeedRepository {

  override suspend fun fetchFeed(feedUrl: String): Result<Feed> =
      fetcher.fetch(feedUrl).andThen { document ->
        withContext(Dispatchers.Default) { parser.parseFetched(document, feedUrl) }
      }
}
