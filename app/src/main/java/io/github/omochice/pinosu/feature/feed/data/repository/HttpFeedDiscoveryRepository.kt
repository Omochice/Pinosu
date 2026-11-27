package io.github.omochice.pinosu.feature.feed.data.repository

import io.github.omochice.pinosu.core.result.andThen
import io.github.omochice.pinosu.feature.feed.data.discovery.FeedLinkDiscoverer
import io.github.omochice.pinosu.feature.feed.data.http.FetchedDocument
import io.github.omochice.pinosu.feature.feed.data.http.HttpDocumentFetcher
import io.github.omochice.pinosu.feature.feed.data.http.HttpsDocumentFetcher
import io.github.omochice.pinosu.feature.feed.data.parser.FeedParser
import io.github.omochice.pinosu.feature.feed.data.parser.UnsupportedFeedFormatException
import io.github.omochice.pinosu.feature.feed.domain.model.FeedCandidate
import io.github.omochice.pinosu.feature.feed.domain.model.FeedResolution
import io.github.omochice.pinosu.feature.feed.domain.model.error.NoFeedFoundException
import io.github.omochice.pinosu.feature.feed.domain.repository.FeedDiscoveryRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

/**
 * Implementation of [FeedDiscoveryRepository] that downloads the URL over HTTPS and, when it is a
 * web page, looks for the feeds it advertises
 *
 * @param okHttpClient Client the pages and feeds are downloaded with
 * @param parser Parser of the downloaded feed documents
 */
class HttpFeedDiscoveryRepository
@Inject
constructor(okHttpClient: OkHttpClient, private val parser: FeedParser) : FeedDiscoveryRepository {

  private val fetcher = HttpsDocumentFetcher(HttpDocumentFetcher(okHttpClient))

  override suspend fun resolve(input: String): Result<FeedResolution> =
      fetcher.fetch(input).andThen { resolveDocument(it) }

  private suspend fun resolveDocument(document: FetchedDocument): Result<FeedResolution> {
    val parsed = withContext(Dispatchers.Default) { parseDirect(document) }
    return if (parsed.exceptionOrNull() is UnsupportedFeedFormatException) {
      resolveCandidates(discover(document), document.finalUrl)
    } else {
      parsed
    }
  }

  private suspend fun resolveCandidates(
      candidates: List<FeedCandidate>,
      pageUrl: String,
  ): Result<FeedResolution> =
      when (candidates.size) {
        0 -> Result.failure(NoFeedFoundException(pageUrl))
        1 -> fetchDirect(candidates.single().url)
        else -> Result.success(FeedResolution.Candidates(candidates))
      }

  private suspend fun fetchDirect(feedUrl: String): Result<FeedResolution> =
      fetcher.fetch(feedUrl).andThen { withContext(Dispatchers.Default) { parseDirect(it) } }

  private fun parseDirect(document: FetchedDocument): Result<FeedResolution> =
      parser.parseFetched(document, document.requestedUrl).map {
        FeedResolution.Direct(document.requestedUrl, it)
      }

  private suspend fun discover(document: FetchedDocument): List<FeedCandidate> =
      withContext(Dispatchers.Default) {
        document.openBody().use {
          FeedLinkDiscoverer.discover(it, document.charset, document.finalUrl)
        }
      }
}
