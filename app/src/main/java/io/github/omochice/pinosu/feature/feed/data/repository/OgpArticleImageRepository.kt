package io.github.omochice.pinosu.feature.feed.data.repository

import io.github.omochice.pinosu.core.metadata.UrlMetadataFetcher
import io.github.omochice.pinosu.feature.feed.data.local.ArticleImageLookup
import io.github.omochice.pinosu.feature.feed.data.local.ArticleImageStore
import io.github.omochice.pinosu.feature.feed.domain.repository.ArticleImageRepository
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Implementation of [ArticleImageRepository] reading the page's Open Graph `og:image`
 *
 * The answer of a page that was read, image or none, is stored and reused for [LOOKUP_INTERVAL], so
 * that reopening the app does not fetch every article page again.
 *
 * An older answer is still given when the page cannot be read again.
 *
 * @param urlMetadataFetcher Reader of the Open Graph tags of a page
 * @param store Where the answers of the pages are kept
 * @param clock Clock deciding whether a stored answer is old enough to be looked up again
 */
class OgpArticleImageRepository
@Inject
constructor(
    private val urlMetadataFetcher: UrlMetadataFetcher,
    private val store: ArticleImageStore,
    private val clock: Clock,
) : ArticleImageRepository {

  override suspend fun findImage(link: String): String? {
    val now = clock.instant()
    val stored = store.find(link)
    return if (stored != null && now < lookedUpAt(stored) + LOOKUP_INTERVAL) {
      stored.imageUrl
    } else {
      lookUpAgain(link, now, stored)
    }
  }

  private suspend fun lookUpAgain(link: String, now: Instant, stale: ArticleImageLookup?): String? =
      lookUp(link)
          .fold(
              onSuccess = { image ->
                store.save(link, ArticleImageLookup(image, now.toEpochMilli()))
                image
              },
              onFailure = { stale?.imageUrl })

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

  private fun lookedUpAt(lookup: ArticleImageLookup): Instant =
      Instant.ofEpochMilli(lookup.lookedUpAtEpochMillis)

  companion object {
    /** How long a stored answer is reused before the page is looked up again */
    val LOOKUP_INTERVAL: Duration = Duration.ofDays(7)
  }
}
