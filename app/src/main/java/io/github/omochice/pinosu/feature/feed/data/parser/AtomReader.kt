package io.github.omochice.pinosu.feature.feed.data.parser

import io.github.omochice.pinosu.feature.feed.domain.model.Feed
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import org.jsoup.nodes.Element

/** Reads Atom (`feed`) documents */
internal object AtomReader {

  private const val TITLE = "title"
  private const val REL = "rel"

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
        summary = entry.childPlainText("summary", "content"),
    )
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
