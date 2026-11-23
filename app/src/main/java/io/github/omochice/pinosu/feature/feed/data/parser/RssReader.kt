package io.github.omochice.pinosu.feature.feed.data.parser

import io.github.omochice.pinosu.feature.feed.domain.model.Feed
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import org.jsoup.nodes.Element

/** Reads RSS 2.0 (`rss`) and RSS 1.0 (`rdf:RDF`) documents */
internal object RssReader {

  private const val TITLE = "title"
  private const val LINK = "link"
  private const val ITEM = "item"
  private const val DESCRIPTION = "description"
  private const val CONTENT_ENCODED = "content:encoded"

  /**
   * Read a feed from the document's root element
   *
   * RSS 2.0 nests items in the channel while RSS 1.0 places them next to it, so both are read.
   *
   * @param root The `rss` or `rdf:RDF` element
   * @param feedUrl URL the feed was fetched from
   */
  fun read(root: Element, feedUrl: String): Feed {
    val channel = root.firstChildNamed("channel")
    val title = channel?.childText(TITLE).orEmpty()
    val itemElements = root.childrenNamed(ITEM) + channel?.childrenNamed(ITEM).orEmpty()
    return Feed(
        url = feedUrl,
        title = title,
        siteUrl = channel?.let { link(it, feedUrl) },
        items = itemElements.map { readItem(it, feedUrl, title) },
    )
  }

  private fun readItem(item: Element, feedUrl: String, feedTitle: String): FeedItem {
    val title = item.childText(TITLE).orEmpty()
    val link = link(item, feedUrl)
    val rawDate = item.childText("pubDate") ?: item.childText("dc:date")
    return FeedItem(
        id = ItemIdentifier.choose(item.childText("guid"), link, title, rawDate),
        feedUrl = feedUrl,
        feedTitle = feedTitle,
        title = title,
        link = link,
        publishedAt = rawDate?.let(FeedDateParser::parse),
        summary = item.childPlainText(DESCRIPTION, CONTENT_ENCODED),
        contentHtml =
            item.childBodyHtml(CONTENT_ENCODED, DESCRIPTION, feedUrl = feedUrl) { it.htmlSource() },
        imageUrl =
            ItemImageFinder.find(item, feedUrl, listOf(CONTENT_ENCODED, DESCRIPTION)) {
              it.htmlSource()
            },
    )
  }

  private fun link(parent: Element, feedUrl: String): String? {
    val element = parent.firstChildNamed(LINK) ?: return null
    return element
        .text()
        .takeIf { it.isNotEmpty() }
        ?.let { LinkResolver.resolve(element, it, feedUrl) }
  }
}
