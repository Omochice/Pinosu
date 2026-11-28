package io.github.omochice.pinosu.feature.feed.data.discovery

import io.github.omochice.pinosu.feature.feed.domain.model.FeedCandidate
import java.io.InputStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/** Finds the RSS and Atom feeds an HTML page advertises with `<link rel="alternate">` */
internal object FeedLinkDiscoverer {

  private val FEED_TYPES = setOf("application/rss+xml", "application/atom+xml")
  private const val ALTERNATE = "alternate"
  private const val LINK_WITH_HREF = "link[href~=\\S]"
  private val WHITESPACE = Regex("\\s+")

  /**
   * Find the feeds advertised by a page
   *
   * @param html Raw bytes of the page; the caller remains responsible for closing it
   * @param charset Charset named by the response, or null to detect it from the page itself
   * @param pageUrl URL the page was served from, which relative links are resolved against
   * @return Advertised feeds in page order, each URL once, with the title of its first link
   */
  fun discover(html: InputStream, charset: String?, pageUrl: String): List<FeedCandidate> =
      Jsoup.parse(html, charset, pageUrl)
          .select(LINK_WITH_HREF)
          .filter(::isFeedAlternate)
          .mapNotNull(::toCandidate)
          .distinctBy { it.url }

  private fun isFeedAlternate(link: Element): Boolean {
    val relTokens = link.attr("rel").lowercase().split(WHITESPACE)
    val mediaType = link.attr("type").substringBefore(';').trim().lowercase()
    return ALTERNATE in relTokens && mediaType in FEED_TYPES
  }

  private fun toCandidate(link: Element): FeedCandidate? =
      link
          .absUrl("href")
          .takeIf { it.isNotBlank() }
          ?.let { FeedCandidate(it, link.attr("title").trim().ifBlank { null }) }
}
