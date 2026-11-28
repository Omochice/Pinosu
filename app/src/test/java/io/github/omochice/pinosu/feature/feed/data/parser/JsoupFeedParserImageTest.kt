package io.github.omochice.pinosu.feature.feed.data.parser

import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JsoupFeedParserImageTest {

  private val parser = JsoupFeedParser()

  @Test
  fun `RSS media thumbnail gives the image`() {
    val item = parseRssItem("""<media:thumbnail url="https://example.com/thumb.jpg"/>""")

    assertEquals("https://example.com/thumb.jpg", item.imageUrl)
  }

  @Test
  fun `media thumbnail is preferred over media content and enclosure and body images`() {
    val item =
        parseRssItem(
            """<media:content url="https://example.com/content.jpg" medium="image"/>""" +
                """<enclosure url="https://example.com/enclosure.jpg" type="image/jpeg"/>""" +
                """<description><![CDATA[<img src="https://example.com/body.jpg">]]></description>""" +
                """<media:thumbnail url="https://example.com/thumb.jpg"/>""")

    assertEquals("https://example.com/thumb.jpg", item.imageUrl)
  }

  @Test
  fun `media content with the image medium is preferred over enclosure and body images`() {
    val item =
        parseRssItem(
            """<enclosure url="https://example.com/enclosure.jpg" type="image/jpeg"/>""" +
                """<description><![CDATA[<img src="https://example.com/body.jpg">]]></description>""" +
                """<media:content url="https://example.com/content.jpg" medium="image"/>""")

    assertEquals("https://example.com/content.jpg", item.imageUrl)
  }

  @Test
  fun `media content with an image MIME type gives the image`() {
    val item =
        parseRssItem("""<media:content url="https://example.com/content.png" type="image/png"/>""")

    assertEquals("https://example.com/content.png", item.imageUrl)
  }

  @Test
  fun `media content that is not an image is skipped`() {
    val item =
        parseRssItem(
            """<media:content url="https://example.com/movie.mp4" medium="video" type="video/mp4"/>""" +
                """<media:content url="https://example.com/untyped.bin"/>""" +
                """<enclosure url="https://example.com/enclosure.jpg" type="image/jpeg"/>""")

    assertEquals("https://example.com/enclosure.jpg", item.imageUrl)
  }

  @Test
  fun `enclosure with an image MIME type is preferred over body images`() {
    val item =
        parseRssItem(
            """<description><![CDATA[<img src="https://example.com/body.jpg">]]></description>""" +
                """<enclosure url="https://example.com/enclosure.jpg" type="image/jpeg"/>""")

    assertEquals("https://example.com/enclosure.jpg", item.imageUrl)
  }

  @Test
  fun `enclosure that is not an image is skipped`() {
    val item =
        parseRssItem(
            """<enclosure url="https://example.com/episode.mp3" type="audio/mpeg"/>""" +
                """<description><![CDATA[<img src="https://example.com/body.jpg">]]></description>""")

    assertEquals("https://example.com/body.jpg", item.imageUrl)
  }

  @Test
  fun `Atom enclosure link with an image type gives the image`() {
    val entry =
        parseAtomEntry(
            """<link rel="alternate" href="https://example.com/post"/>""" +
                """<link rel="enclosure" type="audio/mpeg" href="https://example.com/episode.mp3"/>""" +
                """<link rel="enclosure" type="image/png" href="https://example.com/cover.png"/>""")

    assertEquals("https://example.com/cover.png", entry.imageUrl)
  }

  @Test
  fun `Atom enclosure link without a type is not taken as an image`() {
    val entry = parseAtomEntry("""<link rel="enclosure" href="https://example.com/episode.mp3"/>""")

    assertNull(entry.imageUrl)
  }

  @Test
  fun `Atom media thumbnail nested in a media group gives the image`() {
    val entry =
        parseAtomEntry(
            "<media:group>" +
                """<media:thumbnail url="https://i.example.com/vi/1/hqdefault.jpg"/>""" +
                "</media:group>")

    assertEquals("https://i.example.com/vi/1/hqdefault.jpg", entry.imageUrl)
  }

  @Test
  fun `media thumbnail of a media content that is a video gives the image`() {
    val item =
        parseRssItem(
            """<media:content url="https://example.com/movie.mp4" medium="video">""" +
                """<media:thumbnail url="https://example.com/poster.jpg"/>""" +
                "</media:content>")

    assertEquals("https://example.com/poster.jpg", item.imageUrl)
  }

  @Test
  fun `first image of the RSS body gives the image`() {
    val item =
        parseRssItem(
            "<content:encoded><![CDATA[<p>Intro</p>" +
                """<img src="https://example.com/first.jpg"><img src="https://example.com/second.jpg">""" +
                "]]></content:encoded>")

    assertEquals("https://example.com/first.jpg", item.imageUrl)
  }

  @Test
  fun `RSS body image is looked for in the description when content encoded has none`() {
    val item =
        parseRssItem(
            "<content:encoded><![CDATA[<p>No image here</p>]]></content:encoded>" +
                """<description><![CDATA[<img src="https://example.com/desc.jpg">]]></description>""")

    assertEquals("https://example.com/desc.jpg", item.imageUrl)
  }

  @Test
  fun `image of an Atom html body gives the image`() {
    val entry =
        parseAtomEntry(
            """<content type="html">&lt;p&gt;Hi&lt;/p&gt;&lt;img src="https://example.com/a.png"&gt;</content>""")

    assertEquals("https://example.com/a.png", entry.imageUrl)
  }

  @Test
  fun `image of a body holding only an image is both the image and the body`() {
    val item =
        parseRssItem(
            """<content:encoded><![CDATA[<img src="https://example.com/only.png">]]></content:encoded>""")

    assertEquals("""<img src="https://example.com/only.png">""", item.contentHtml)
    assertEquals("https://example.com/only.png", item.imageUrl)
  }

  @Test
  fun `relative media thumbnail resolves against the feed url`() {
    val item =
        parseRssItem(
            """<media:thumbnail url="images/thumb.jpg"/>""",
            feedUrl = "https://example.com/blog/feed.xml")

    assertEquals("https://example.com/blog/images/thumb.jpg", item.imageUrl)
  }

  @Test
  fun `relative body image resolves against the xml_base of the content`() {
    val entry =
        parseFeed(
                """
                <feed xmlns="http://www.w3.org/2005/Atom" xml:base="https://base.example.com/blog/">
                  <entry xml:base="2024/">
                    <id>1</id>
                    <content type="html" xml:base="post/">&lt;img src="cover.png"&gt;</content>
                  </entry>
                </feed>
                """
                    .trimIndent())
            .items
            .single()

    assertEquals("https://base.example.com/blog/2024/post/cover.png", entry.imageUrl)
  }

  @Test
  fun `image with a scheme other than http and https is skipped for the next source`() {
    val item =
        parseRssItem(
            """<media:thumbnail url="javascript:alert(1)"/>""" +
                """<enclosure url="intent://scan/#Intent;scheme=zxing;end" type="image/png"/>""" +
                "<description><![CDATA[" +
                """<img src="data:image/png;base64,AAAA"><img src="https://example.com/ok.png">""" +
                "]]></description>")

    assertEquals("https://example.com/ok.png", item.imageUrl)
  }

  @Test
  fun `only images with schemes other than http and https give no image`() {
    val item =
        parseRssItem(
            """<media:thumbnail url="ftp://example.com/thumb.jpg"/>""" +
                """<description><![CDATA[<img src="data:image/png;base64,AAAA">]]></description>""")

    assertNull(item.imageUrl)
  }

  @Test
  fun `item without any image has no image`() {
    val item =
        parseRssItem(
            "<title>Plain</title><description><![CDATA[<p>Text only</p>]]></description>" +
                """<enclosure url="https://example.com/episode.mp3" type="audio/mpeg"/>""")

    assertNull(item.imageUrl)
  }

  @Test
  fun `blank image urls are skipped`() {
    val item =
        parseRssItem(
            """<media:thumbnail url="  "/><media:content medium="image"/>""" +
                """<enclosure url="https://example.com/enclosure.jpg" type="image/jpeg"/>""")

    assertEquals("https://example.com/enclosure.jpg", item.imageUrl)
  }

  private fun parseFeed(xml: String, feedUrl: String = FEED_URL) =
      parser.parse(xml.byteInputStream(), feedUrl).getOrThrow()

  private fun parseRssItem(itemBody: String, feedUrl: String = FEED_URL): FeedItem =
      parseFeed(
              """<rss version="2.0" xmlns:media="http://search.yahoo.com/mrss/" """ +
                  """xmlns:content="http://purl.org/rss/1.0/modules/content/">""" +
                  "<channel><title>T</title><item>$itemBody</item></channel></rss>",
              feedUrl)
          .items
          .single()

  private fun parseAtomEntry(entryBody: String): FeedItem =
      parseFeed(
              """<feed xmlns="http://www.w3.org/2005/Atom" xmlns:media="http://search.yahoo.com/mrss/">""" +
                  "<title>T</title><entry><id>1</id>$entryBody</entry></feed>")
          .items
          .single()

  companion object {
    private const val FEED_URL = "https://example.com/feed.xml"
  }
}
