package io.github.omochice.pinosu.feature.feed.data.parser

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.nodes.Element

/** Turns links found in a feed into absolute URLs, honoring `xml:base` */
internal object LinkResolver {

  private const val XML_BASE = "xml:base"

  /**
   * Resolve a link found on or inside [element]
   *
   * The base is [feedUrl], refined by every `xml:base` from the root down to [element] itself.
   *
   * @param element Element the link was read from
   * @param link Link as written in the feed
   * @param feedUrl URL the feed was fetched from
   * @return The absolute HTTP(S) URL, or null when [link] does not resolve to one
   */
  fun resolve(element: Element, link: String, feedUrl: String): String? =
      resolveAgainst(baseOf(element, feedUrl), link)

  /**
   * Base URL that links on or inside [element] are relative to
   *
   * @param element Element whose links are resolved
   * @param feedUrl URL the feed was fetched from
   * @return [feedUrl] refined by every `xml:base` from the root down to [element] itself
   */
  fun baseOf(element: Element, feedUrl: String): String =
      (element.parents().asReversed() + element)
          .map { it.attr(XML_BASE) }
          .filter { it.isNotBlank() }
          .fold(feedUrl) { current, xmlBase -> resolveAgainst(current, xmlBase) ?: current }

  /**
   * Resolve [link] against [base]
   *
   * Feeds come from arbitrary hosts, so a link with any other scheme (`intent:`, `nostrsigner:`,
   * `javascript:`) is rejected rather than kept, because opening it would hand control to another
   * app.
   *
   * @return The absolute HTTP(S) URL, or null when [link] does not resolve to one
   */
  fun resolveAgainst(base: String, link: String): String? =
      (base.toHttpUrlOrNull()?.resolve(link) ?: link.toHttpUrlOrNull())?.toString()
}
