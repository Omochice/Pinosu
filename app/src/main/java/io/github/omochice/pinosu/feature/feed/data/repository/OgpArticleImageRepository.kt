package io.github.omochice.pinosu.feature.feed.data.repository

import io.github.omochice.pinosu.core.metadata.UrlMetadataFetcher
import io.github.omochice.pinosu.feature.feed.domain.repository.ArticleImageRepository
import javax.inject.Inject
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Implementation of [ArticleImageRepository] reading the page's Open Graph `og:image`
 *
 * @param urlMetadataFetcher Fetcher of the page's Open Graph metadata
 */
class OgpArticleImageRepository
@Inject
constructor(private val urlMetadataFetcher: UrlMetadataFetcher) : ArticleImageRepository {

  /**
   * The scheme is checked here rather than in [UrlMetadataFetcher], whose bookmark callers would
   * otherwise change behaviour; pages come from arbitrary hosts, so a `data:` or `intent:` image
   * must not reach the image loader.
   */
  override suspend fun findImage(link: String): String? =
      urlMetadataFetcher.fetchMetadata(link).getOrNull()?.imageUrl?.takeIf {
        it.toHttpUrlOrNull() != null
      }
}
