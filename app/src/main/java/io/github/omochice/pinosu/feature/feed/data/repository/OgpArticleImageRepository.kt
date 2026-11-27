package io.github.omochice.pinosu.feature.feed.data.repository

import io.github.omochice.pinosu.core.metadata.UrlMetadataFetcher
import io.github.omochice.pinosu.feature.feed.domain.repository.ArticleImageRepository
import javax.inject.Inject
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/** Implementation of [ArticleImageRepository] reading the page's Open Graph `og:image` */
class OgpArticleImageRepository
@Inject
constructor(private val urlMetadataFetcher: UrlMetadataFetcher) : ArticleImageRepository {

  override suspend fun findImage(link: String): String? = lookUp(link).getOrNull()

  /**
   * The scheme is checked here rather than in [UrlMetadataFetcher], whose bookmark callers would
   * otherwise change behaviour; pages come from arbitrary hosts, so a `data:` or `intent:` image
   * must not reach the image loader.
   *
   * @return The image of a page that was read, null for a page that declares none, or a failure
   *   when the page could not be read
   */
  private suspend fun lookUp(link: String): Result<String?> =
      urlMetadataFetcher.fetchMetadata(link).map { metadata ->
        metadata.imageUrl?.takeIf { it.toHttpUrlOrNull() != null }
      }
}
