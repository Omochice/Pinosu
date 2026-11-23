package io.github.omochice.pinosu.feature.feed.data.parser

import io.github.omochice.pinosu.feature.feed.domain.model.Feed
import java.io.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import javax.inject.Inject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser

/** Parses an RSS 2.0, RSS 1.0, or Atom feed document */
interface FeedParser {

  /**
   * Parse a feed document
   *
   * The caller remains responsible for closing [body].
   *
   * @param body Raw bytes of the document; the charset is detected from the byte order mark or the
   *   XML declaration
   * @param feedUrl URL the document was fetched from, used to resolve relative links
   * @return The parsed feed, or a failure with [UnsupportedFeedFormatException] when the document
   *   is not a feed, or with an I/O exception when the stream cannot be read
   */
  fun parse(body: InputStream, feedUrl: String): Result<Feed>
}

/** Implementation of [FeedParser] using Jsoup's lenient XML parser */
class JsoupFeedParser @Inject constructor() : FeedParser {

  override fun parse(body: InputStream, feedUrl: String): Result<Feed> =
      try {
        read(Jsoup.parse(body, null, feedUrl, Parser.xmlParser()).children().firstOrNull(), feedUrl)
      } catch (e: IOException) {
        Result.failure(e)
      } catch (e: UncheckedIOException) {
        Result.failure(e)
      }

  private fun read(root: Element?, feedUrl: String): Result<Feed> =
      when {
        root == null -> Result.failure(UnsupportedFeedFormatException(null))
        root.isNamed(RSS_2_0_ROOT) || root.isNamed(RSS_1_0_ROOT) ->
            Result.success(RssReader.read(root, feedUrl))
        root.isNamed(ATOM_ROOT) -> Result.success(AtomReader.read(root, feedUrl))
        else -> Result.failure(UnsupportedFeedFormatException(root.tagName()))
      }

  companion object {
    private const val RSS_2_0_ROOT = "rss"
    private const val RSS_1_0_ROOT = "rdf:RDF"
    private const val ATOM_ROOT = "feed"
  }
}
