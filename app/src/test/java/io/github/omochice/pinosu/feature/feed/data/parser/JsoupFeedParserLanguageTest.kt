package io.github.omochice.pinosu.feature.feed.data.parser

import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JsoupFeedParserLanguageTest {

  private val parser = JsoupFeedParser()

  @Test
  fun `RSS 2_0 items take the language of the channel`() {
    val items =
        parseItems(
            """
            <rss version="2.0"><channel><title>T</title><language>ja-jp</language>
              <item><title>A</title></item>
              <item><title>B</title></item>
            </channel></rss>
            """
                .trimIndent())

    assertEquals(listOf("ja-jp", "ja-jp"), items.map { it.language })
  }

  @Test
  fun `RSS language is trimmed and written with hyphens`() {
    val item =
        parseItems(
                """
                <rss version="2.0"><channel><title>T</title><language> en_US </language>
                  <item><title>A</title></item>
                </channel></rss>
                """
                    .trimIndent())
            .single()

    assertEquals("en-US", item.language)
  }

  @Test
  fun `RSS 1_0 items take dc_language of the channel unless they declare their own`() {
    val items =
        parseItems(
            """
            <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns="http://purl.org/rss/1.0/" xmlns:dc="http://purl.org/dc/elements/1.1/">
              <channel rdf:about="https://example.com/rss"><title>T</title>
                <dc:language>ja</dc:language>
              </channel>
              <item rdf:about="https://example.com/1"><title>A</title></item>
              <item rdf:about="https://example.com/2"><title>B</title>
                <dc:language>en</dc:language>
              </item>
            </rdf:RDF>
            """
                .trimIndent())

    assertEquals(listOf("ja", "en"), items.map { it.language })
  }

  @Test
  fun `Atom entries take xml_lang of their own, else of the feed`() {
    val items =
        parseItems(
            """
            <feed xmlns="http://www.w3.org/2005/Atom" xml:lang="ja">
              <title>T</title>
              <entry><title>A</title><id>1</id></entry>
              <entry xml:lang="en-GB"><title>B</title><id>2</id></entry>
            </feed>
            """
                .trimIndent())

    assertEquals(listOf("ja", "en-GB"), items.map { it.language })
  }

  @Test
  fun `items of a feed that declares no language have none`() {
    val rss =
        parseItems(
                """<rss version="2.0"><channel><title>T</title><item><title>A</title></item></channel></rss>""")
            .single()
    val atom =
        parseItems(
                """<feed xmlns="http://www.w3.org/2005/Atom"><title>T</title>""" +
                    """<entry><title>A</title><id>1</id></entry></feed>""")
            .single()

    assertNull(rss.language)
    assertNull(atom.language)
  }

  @Test
  fun `a blank language counts as none`() {
    val item =
        parseItems(
                """
                <rss version="2.0"><channel><title>T</title><language>  </language>
                  <item><title>A</title></item>
                </channel></rss>
                """
                    .trimIndent())
            .single()

    assertNull(item.language)
  }

  private fun parseItems(xml: String): List<FeedItem> =
      parser.parse(xml.byteInputStream(), FEED_URL).getOrThrow().items

  companion object {
    private const val FEED_URL = "https://example.com/feed.xml"
  }
}
