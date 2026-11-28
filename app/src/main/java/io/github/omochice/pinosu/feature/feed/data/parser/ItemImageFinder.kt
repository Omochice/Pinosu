package io.github.omochice.pinosu.feature.feed.data.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/** Finds the image a feed gives one of its items */
internal object ItemImageFinder {

  private const val URL = "url"
  private const val TYPE = "type"
  private const val MEDIA_CONTENT = "media:content"
  private const val MEDIA_GROUP = "media:group"
  private const val IMAGE_TYPE_PREFIX = "image/"

  /**
   * Find the image of an item
   *
   * Sources in order of preference: a Media RSS thumbnail, a Media RSS content that is an image, an
   * RSS `enclosure` or Atom `link rel="enclosure"` with an image type, and the first image in the
   * body. A source whose URL does not resolve to an HTTP(S) URL gives way to the next.
   *
   * @param item The `item` or `entry` element
   * @param feedUrl URL the feed was fetched from
   * @param bodyNames Names of the children that may hold the body, in order of preference
   * @param htmlOf HTML a body child holds, or null when it holds nothing that can be shown
   * @return The absolute HTTP(S) URL of the image, or null when the item has none
   */
  fun find(
      item: Element,
      feedUrl: String,
      bodyNames: List<String>,
      htmlOf: (Element) -> String?,
  ): String? =
      declaredImages(item).firstNotNullOfOrNull { (element, url) ->
        LinkResolver.resolve(element, url, feedUrl)
      } ?: bodyImage(item, feedUrl, bodyNames, htmlOf)

  private fun declaredImages(item: Element): Sequence<Pair<Element, String>> {
    val groups = listOf(item) + item.childrenNamed(MEDIA_GROUP)
    val contents = groups.flatMap { it.childrenNamed(MEDIA_CONTENT) }
    val thumbnails = (groups + contents).flatMap { it.childrenNamed("media:thumbnail") }
    val enclosureLinks = item.childrenNamed("link").filter { it.attr("rel") == "enclosure" }
    return sequenceOf(
            thumbnails.map { it to it.attr(URL) },
            contents.filter(::isImageContent).map { it to it.attr(URL) },
            item.childrenNamed("enclosure").filter(::hasImageType).map { it to it.attr(URL) },
            enclosureLinks.filter(::hasImageType).map { it to it.attr("href") },
        )
        .flatten()
  }

  private fun isImageContent(content: Element): Boolean =
      content.attr("medium") == "image" || hasImageType(content)

  private fun hasImageType(element: Element): Boolean =
      element.attr(TYPE).trim().startsWith(IMAGE_TYPE_PREFIX, ignoreCase = true)

  /**
   * First image in the raw body, read from every body child because the body shown is only the
   * first of them that has anything left once sanitized
   */
  private fun bodyImage(
      item: Element,
      feedUrl: String,
      bodyNames: List<String>,
      htmlOf: (Element) -> String?,
  ): String? =
      bodyNames.asSequence().mapNotNull(item::firstChildNamed).firstNotNullOfOrNull { child ->
        htmlOf(child)?.let { firstImage(it, LinkResolver.baseOf(child, feedUrl)) }
      }

  private fun firstImage(html: String, base: String): String? =
      Jsoup.parseBodyFragment(html).select("img[src]").firstNotNullOfOrNull { image ->
        LinkResolver.resolveWritten(base, image.attr("src"))
      }
}
