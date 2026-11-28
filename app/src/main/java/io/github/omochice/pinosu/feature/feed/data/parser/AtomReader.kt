package io.github.omochice.pinosu.feature.feed.data.parser

import io.github.omochice.pinosu.feature.feed.domain.model.Feed
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities

/** Reads Atom (`feed`) documents */
internal object AtomReader {

  private const val TITLE = "title"
  private const val REL = "rel"
  private const val SUMMARY = "summary"
  private const val CONTENT = "content"
  private const val TEXT_TYPE = "text"

  /**
   * Read a feed from the document's root element
   *
   * @param root The `feed` element
   * @param feedUrl URL the feed was fetched from
   */
  fun read(root: Element, feedUrl: String): Feed {
    val title = root.childText(TITLE).orEmpty()
    return Feed(
        url = feedUrl,
        title = title,
        siteUrl = alternateLink(root, feedUrl),
        items = root.childrenNamed("entry").map { readEntry(it, feedUrl, title) },
    )
  }

  private fun readEntry(entry: Element, feedUrl: String, feedTitle: String): FeedItem {
    val title = entry.childText(TITLE).orEmpty()
    val link = alternateLink(entry, feedUrl)
    val rawDate = entry.childText("published") ?: entry.childText("updated")
    return FeedItem(
        id = ItemIdentifier.choose(entry.childText("id"), link, title, rawDate),
        feedUrl = feedUrl,
        feedTitle = feedTitle,
        title = title,
        link = link,
        publishedAt = rawDate?.let(FeedDateParser::parse),
        summary = entry.childPlainText(SUMMARY, CONTENT),
        contentHtml = entry.childBodyHtml(CONTENT, SUMMARY, feedUrl = feedUrl, htmlOf = ::textHtml),
        imageUrl = ItemImageFinder.find(entry, feedUrl, listOf(CONTENT, SUMMARY), ::textHtml),
    )
  }

  /**
   * HTML of an Atom text construct, decided by its `type` because plain text may contain `<` that
   * must not be read as markup
   *
   * @return The HTML, or null for any other media type, which is either out of line or binary
   */
  private fun textHtml(element: Element): String? =
      when (element.attr("type").ifEmpty { TEXT_TYPE }) {
        TEXT_TYPE -> Entities.escape(element.wholeText())
        "html" -> element.wholeText()
        "xhtml" -> element.html()
        else -> null
      }

  private fun alternateLink(parent: Element, feedUrl: String): String? {
    val links = parent.childrenNamed("link")
    val element =
        links.firstOrNull { it.attr(REL) == "alternate" }
            ?: links.firstOrNull { !it.hasAttr(REL) }
            ?: return null
    return element
        .attr("href")
        .trim()
        .takeIf { it.isNotEmpty() }
        ?.let { LinkResolver.resolve(element, it, feedUrl) }
  }
}
