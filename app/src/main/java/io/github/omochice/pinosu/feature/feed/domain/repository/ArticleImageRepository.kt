package io.github.omochice.pinosu.feature.feed.domain.repository

/** Finds the image a web article declares for itself */
interface ArticleImageRepository {

  /**
   * Find the image of an article
   *
   * @param link Absolute URL of the article page
   * @return Absolute HTTP(S) URL of the image, or null when the page declares none or cannot be
   *   fetched
   */
  suspend fun findImage(link: String): String?
}
