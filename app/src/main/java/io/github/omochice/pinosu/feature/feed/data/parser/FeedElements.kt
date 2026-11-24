package io.github.omochice.pinosu.feature.feed.data.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/** Whether this element's qualified name, including any namespace prefix, is [name] */
internal fun Element.isNamed(name: String): Boolean = tagName().equals(name, ignoreCase = true)

/** Direct children named [name], in document order */
internal fun Element.childrenNamed(name: String): List<Element> =
    children().filter { it.isNamed(name) }

/** First direct child named [name] */
internal fun Element.firstChildNamed(name: String): Element? =
    children().firstOrNull { it.isNamed(name) }

/**
 * Text of the first direct child named [name], with whitespace collapsed and trimmed
 *
 * @return The text, or null when the child is missing or blank
 */
internal fun Element.childText(name: String): String? =
    firstChildNamed(name)?.text()?.takeIf { it.isNotEmpty() }

/**
 * Plain text of the first direct child among [names] whose content, read as HTML, has any text
 *
 * @return The text, or an empty string when none of the children has any
 */
internal fun Element.childPlainText(vararg names: String): String =
    names
        .firstNotNullOfOrNull { name ->
          firstChildNamed(name)
              ?.let { Jsoup.parse(it.htmlSource()).text() }
              ?.takeIf { it.isNotEmpty() }
        }
        .orEmpty()

/**
 * Sanitized body of the first child among [names] with text, else of the first with an image
 *
 * A body holding only images gives way to a later one with text, because the text carries the
 * article while the image is often shown as the image of the item already.
 *
 * @param names Names of the children to try, in order of preference
 * @param feedUrl URL the feed was fetched from, used to resolve relative links
 * @param htmlOf HTML a child holds, or null when it holds nothing that can be shown
 * @return The body, or null when none of the children has any text or image
 */
internal fun Element.childBodyHtml(
    names: List<String>,
    feedUrl: String,
    htmlOf: (Element) -> String?,
): String? {
  val bodies =
      names.asSequence().mapNotNull(::firstChildNamed).mapNotNull { child ->
        htmlOf(child)?.let { ItemBodySanitizer.sanitize(it, LinkResolver.baseOf(child, feedUrl)) }
      }
  return (bodies.firstOrNull { it.hasText } ?: bodies.firstOrNull())?.html
}

/** HTML held by this element, whether written as markup or as escaped text */
internal fun Element.htmlSource(): String = if (childrenSize() > 0) html() else wholeText()
