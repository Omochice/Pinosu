package io.github.omochice.pinosu.feature.feed.data.parser

import io.github.omochice.pinosu.feature.feed.domain.model.Feed
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JsoupFeedParserTest {

  private val parser = JsoupFeedParser()

  @Test
  fun `RSS 2_0 channel gives the feed title and site url, ignoring image and atom self links`() {
    val feed = parseFeed(RSS_2_0)

    assertEquals(FEED_URL, feed.url)
    assertEquals("Example Blog", feed.title)
    assertEquals("https://example.com/", feed.siteUrl)
  }

  @Test
  fun `RSS 2_0 items keep document order and carry every field`() {
    val feed = parseFeed(RSS_2_0)

    assertEquals(listOf("First post", "Second post"), feed.items.map { it.title })
    val first = feed.items.first()
    assertEquals("urn:example:1", first.id)
    assertEquals(FEED_URL, first.feedUrl)
    assertEquals("Example Blog", first.feedTitle)
    assertEquals("https://example.com/posts/1", first.link)
    assertEquals(Instant.parse("2024-01-01T18:04:05Z"), first.publishedAt)
    assertEquals("Hello world", first.summary)
  }

  @Test
  fun `RSS 1_0 reads items that are siblings of the channel and dates from dc_date`() {
    val feed = parseFeed(RSS_1_0)

    assertEquals("RDF Blog", feed.title)
    assertEquals("https://rdf.example.com/", feed.siteUrl)
    assertEquals(listOf("RDF first", "RDF second"), feed.items.map { it.title })
    val first = feed.items.first()
    assertEquals("https://rdf.example.com/1", first.link)
    assertEquals("https://rdf.example.com/1", first.id)
    assertEquals(Instant.parse("2024-01-01T18:04:05Z"), first.publishedAt)
    assertEquals("RDF summary", first.summary)
  }

  @Test
  fun `Atom feed gives the title and the alternate link as site url`() {
    val feed = parseFeed(ATOM)

    assertEquals("Atom Blog", feed.title)
    assertEquals("https://atom.example.com/", feed.siteUrl)
  }

  @Test
  fun `Atom entry uses id, the alternate link among several, published date, and summary`() {
    val entry = parseFeed(ATOM).items.first()

    assertEquals("tag:atom.example.com,2024:1", entry.id)
    assertEquals("Atom first", entry.title)
    assertEquals("https://atom.example.com/1", entry.link)
    assertEquals(Instant.parse("2024-01-02T03:04:05Z"), entry.publishedAt)
    assertEquals("Atom summary", entry.summary)
  }

  @Test
  fun `Atom entry without published uses updated, a link without rel, and content`() {
    val entry = parseFeed(ATOM).items[1]

    assertEquals("https://atom.example.com/2", entry.link)
    assertEquals(Instant.parse("2024-01-01T18:04:05Z"), entry.publishedAt)
    assertEquals("Body text", entry.summary)
  }

  @Test
  fun `Atom relative links resolve against nested xml_base`() {
    val feed =
        parseFeed(
            """
            <feed xmlns="http://www.w3.org/2005/Atom" xml:base="https://base.example.com/blog/">
              <title>Base</title>
              <link rel="alternate" href="./"/>
              <entry xml:base="2024/">
                <id>1</id>
                <link href="post.html"/>
              </entry>
              <entry>
                <id>2</id>
                <link href="/about"/>
              </entry>
            </feed>
            """
                .trimIndent())

    assertEquals("https://base.example.com/blog/", feed.siteUrl)
    assertEquals(
        listOf("https://base.example.com/blog/2024/post.html", "https://base.example.com/about"),
        feed.items.map { it.link })
  }

  @Test
  fun `relative links without xml_base resolve against the feed url`() {
    val feed =
        parseFeed(
            """
            <rss version="2.0"><channel>
              <title>Relative</title>
              <link>/</link>
              <item><title>A</title><link>posts/a.html</link></item>
            </channel></rss>
            """
                .trimIndent(),
            feedUrl = "https://example.com/blog/feed.xml")

    assertEquals("https://example.com/", feed.siteUrl)
    assertEquals("https://example.com/blog/posts/a.html", feed.items.single().link)
  }

  @Test
  fun `a link that is not a valid url does not fail the feed`() {
    val feed =
        parseFeed(
            """
            <rss version="2.0"><channel>
              <title>Odd</title>
              <item><title>A</title><link>mailto:someone@example.com</link></item>
              <item><title>B</title><link>posts/a b.html</link></item>
            </channel></rss>
            """
                .trimIndent())

    assertEquals(2, feed.items.size)
    assertNull(feed.items[0].link)
  }

  @Test
  fun `links with schemes other than http and https are dropped so a feed cannot launch other apps`() {
    val feed =
        parseFeed(
            """
            <rss version="2.0"><channel>
              <title>Hostile</title>
              <link>intent://scan/#Intent;scheme=zxing;end</link>
              <item><title>Signer</title><link>nostrsigner:{"kind":1}</link></item>
              <item><title>Script</title><link>javascript:alert(1)</link></item>
              <item><title>Plain</title><link>http://example.com/a</link></item>
            </channel></rss>
            """
                .trimIndent())

    assertNull(feed.siteUrl)
    assertEquals(listOf(null, null, "http://example.com/a"), feed.items.map { it.link })
  }

  @Test
  fun `atom links with schemes other than http and https are dropped`() {
    val feed =
        parseFeed(
            """
            <feed xmlns="http://www.w3.org/2005/Atom">
              <title>Hostile</title>
              <link rel="alternate" href="intent://x#Intent;end"/>
              <entry><id>urn:1</id><title>A</title><link rel="alternate" href="nostrsigner:abc"/></entry>
            </feed>
            """
                .trimIndent())

    assertNull(feed.siteUrl)
    assertNull(feed.items.single().link)
  }

  @Test
  fun `CDATA titles and HTML entities such as nbsp are read as plain text`() {
    val feed =
        parseFeed(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0"><channel>
              <title><![CDATA[Tom & Jerry's]]></title>
              <item>
                <title>Hello&nbsp;World &amp; more</title>
                <description><![CDATA[<p>First&nbsp;line</p><p>Second <b>bold</b></p>]]></description>
              </item>
              <item>
                <title><![CDATA[  Padded title  ]]></title>
                <description>&lt;p&gt;Escaped &amp;amp; HTML&lt;/p&gt;</description>
              </item>
            </channel></rss>
            """
                .trimIndent())

    assertEquals("Tom & Jerry's", feed.title)
    assertEquals(listOf("Hello World & more", "Padded title"), feed.items.map { it.title })
    assertEquals(listOf("First line Second bold", "Escaped & HTML"), feed.items.map { it.summary })
  }

  @Test
  fun `RSS item without description uses content encoded`() {
    val feed =
        parseFeed(
            """
            <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/"><channel>
              <title>Content</title>
              <item><title>A</title><content:encoded><![CDATA[<p>Full text</p>]]></content:encoded></item>
            </channel></rss>
            """
                .trimIndent())

    assertEquals("Full text", feed.items.single().summary)
  }

  @Test
  fun `missing title, summary, link, and date give empty strings and nulls`() {
    val feed =
        parseFeed(
            """
            <rss version="2.0"><channel>
              <item><guid>only-guid</guid></item>
            </channel></rss>
            """
                .trimIndent())

    assertEquals("", feed.title)
    assertNull(feed.siteUrl)
    val item = feed.items.single()
    assertEquals("", item.title)
    assertEquals("", item.summary)
    assertNull(item.link)
    assertNull(item.publishedAt)
  }

  @Test
  fun `unparsable date gives null`() {
    val item = parseFeed(rssItem("<title>A</title><pubDate>someday</pubDate>")).items.single()

    assertNull(item.publishedAt)
  }

  @Test
  fun `guid is used verbatim as the identifier even when it looks like a relative url`() {
    val item =
        parseFeed(rssItem("<guid isPermaLink=\"false\">123</guid><link>/a</link>")).items.single()

    assertEquals("123", item.id)
  }

  @Test
  fun `blank guid falls back to the link`() {
    val item =
        parseFeed(rssItem("<guid>  </guid><link>https://example.com/a</link>")).items.single()

    assertEquals("https://example.com/a", item.id)
  }

  @Test
  fun `Atom entry without id falls back to the link`() {
    val feed =
        parseFeed(
            """
            <feed xmlns="http://www.w3.org/2005/Atom">
              <entry><title>A</title><link href="https://example.com/a"/></entry>
            </feed>
            """
                .trimIndent())

    assertEquals("https://example.com/a", feed.items.single().id)
  }

  @Test
  fun `item without guid or link gets a hash identifier that is stable across parses`() {
    val xml = rssItem("<title>A</title><pubDate>Tue, 02 Jan 2024 03:04:05 GMT</pubDate>")

    val first = parseFeed(xml).items.single().id
    val second = parseFeed(xml).items.single().id

    assertTrue(first.isNotBlank())
    assertEquals(first, second)
  }

  @Test
  fun `hash identifier differs for different titles and for different dates`() {
    val base =
        parseFeed(rssItem("<title>A</title><pubDate>Tue, 02 Jan 2024 03:04:05 GMT</pubDate>"))
    val otherTitle =
        parseFeed(rssItem("<title>B</title><pubDate>Tue, 02 Jan 2024 03:04:05 GMT</pubDate>"))
    val otherDate =
        parseFeed(rssItem("<title>A</title><pubDate>Wed, 03 Jan 2024 03:04:05 GMT</pubDate>"))

    assertNotEquals(base.items.single().id, otherTitle.items.single().id)
    assertNotEquals(base.items.single().id, otherDate.items.single().id)
  }

  @Test
  fun `Shift_JIS bytes are decoded using the XML declaration`() {
    val xml =
        """
        <?xml version="1.0" encoding="Shift_JIS"?>
        <rss version="2.0"><channel>
          <title>日本語のブログ</title>
          <item><title>こんにちは世界</title></item>
        </channel></rss>
        """
            .trimIndent()

    val feed =
        parser
            .parse(xml.toByteArray(Charset.forName("Shift_JIS")).inputStream(), FEED_URL)
            .getOrThrow()

    assertEquals("日本語のブログ", feed.title)
    assertEquals("こんにちは世界", feed.items.single().title)
  }

  @Test
  fun `UTF-8 bytes after a byte order mark are decoded`() {
    val xml = "﻿<?xml version=\"1.0\"?><rss><channel><title>日本語</title></channel></rss>"

    val feed = parser.parse(xml.toByteArray(Charsets.UTF_8).inputStream(), FEED_URL).getOrThrow()

    assertEquals("日本語", feed.title)
  }

  @Test
  fun `UTF-16 bytes after a byte order mark are decoded`() {
    val xml = "﻿<rss><channel><title>日本語</title></channel></rss>"

    val feed = parser.parse(xml.toByteArray(Charsets.UTF_16BE).inputStream(), FEED_URL).getOrThrow()

    assertEquals("日本語", feed.title)
  }

  @Test
  fun `Atom xhtml content keeps the separation between block elements`() {
    val feed =
        parseFeed(
            """
            <feed xmlns="http://www.w3.org/2005/Atom">
              <entry>
                <id>1</id>
                <content type="xhtml"><div xmlns="http://www.w3.org/1999/xhtml"><p>One</p><p>Two</p></div></content>
              </entry>
            </feed>
            """
                .trimIndent())

    assertEquals("One Two", feed.items.single().summary)
  }

  @Test
  fun `RSS body prefers content encoded while the summary still prefers description`() {
    val item =
        parseFeed(
                rssItem(
                    "<description>Short</description>" +
                        "<content:encoded><![CDATA[<p>Full <b>text</b></p>]]></content:encoded>"))
            .items
            .single()

    assertEquals("Short", item.summary)
    assertEquals("<p>Full <b>text</b></p>", item.contentHtml)
  }

  @Test
  fun `RSS body falls back to description written as escaped HTML`() {
    val item = parseFeed(RSS_2_0).items.first()

    assertEquals("<p>Hello <b>world</b></p>", item.contentHtml)
  }

  @Test
  fun `RSS body falls back to description when content encoded has neither text nor an image left`() {
    val item =
        parseFeed(
                rssItem(
                    "<description>Short</description>" +
                        """<content:encoded><![CDATA[<img src="javascript:alert(1)">]]></content:encoded>"""))
            .items
            .single()

    assertEquals("Short", item.contentHtml)
  }

  @Test
  fun `Atom body prefers html content over summary while the summary still prefers summary`() {
    val entry = parseFeed(ATOM).items.first()

    assertEquals("Atom summary", entry.summary)
    assertEquals("<p>Ignored content</p>", entry.contentHtml)
  }

  @Test
  fun `Atom xhtml content keeps its inline markup as the body`() {
    val entry =
        parseFeed(
                """
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <entry>
                    <id>1</id>
                    <content type="xhtml"><div xmlns="http://www.w3.org/1999/xhtml"><p>One <em>two</em></p></div></content>
                  </entry>
                </feed>
                """
                    .trimIndent())
            .items
            .single()

    assertEquals("<div><p>One <em>two</em></p></div>", entry.contentHtml)
  }

  @Test
  fun `Atom text content and content without a type are escaped so markup shows as text`() {
    val entries =
        parseFeed(
                """
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <entry><id>1</id><content type="text">1 &lt; 2 &lt;b&gt;not bold&lt;/b&gt;</content></entry>
                  <entry><id>2</id><content>&lt;i&gt;plain&lt;/i&gt;</content></entry>
                </feed>
                """
                    .trimIndent())
            .items

    assertEquals(
        listOf("1 &lt; 2 &lt;b&gt;not bold&lt;/b&gt;", "&lt;i&gt;plain&lt;/i&gt;"),
        entries.map { it.contentHtml })
  }

  @Test
  fun `Atom body falls back to an html summary when there is no content`() {
    val entry =
        parseFeed(
                """
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <entry><id>1</id><summary type="html">&lt;p&gt;From summary&lt;/p&gt;</summary></entry>
                </feed>
                """
                    .trimIndent())
            .items
            .single()

    assertEquals("<p>From summary</p>", entry.contentHtml)
  }

  @Test
  fun `body keeps the separation between headings and blocks`() {
    val item =
        parseFeed(
                rssItem(
                    "<content:encoded><![CDATA[<h2>Title</h2><div>One</div><div>Two</div>" +
                        "<ul><li>Item</li></ul><blockquote>Quote</blockquote>" +
                        "<pre><code>val x = 1</code></pre>]]></content:encoded>"))
            .items
            .single()

    assertEquals(
        "<h2>Title</h2><div>One</div><div>Two</div><ul><li>Item</li></ul>" +
            "<blockquote>Quote</blockquote><pre><code>val x = 1</code></pre>",
        item.contentHtml)
  }

  @Test
  fun `body drops scripts, styles, iframes, forms, and event attributes`() {
    val item =
        parseFeed(
                rssItem(
                    "<content:encoded><![CDATA[" +
                        """<p onclick="steal()" style="color:red">Hi<script>alert(1)</script></p>""" +
                        "<style>p { display: none }</style>" +
                        """<iframe src="https://evil.example/"></iframe>""" +
                        """<form action="https://evil.example/"><input name="q"></form>""" +
                        """<img src="https://example.com/a.png" onerror="steal()">""" +
                        "]]></content:encoded>"))
            .items
            .single()

    assertEquals("""<p>Hi</p><img src="https://example.com/a.png">""", item.contentHtml)
  }

  @Test
  fun `relative links in the body resolve against the feed url`() {
    val item =
        parseFeed(
                rssItem(
                    """<content:encoded><![CDATA[<a href="/about">About</a> """ +
                        """<a href="next.html">Next</a>]]></content:encoded>"""),
                feedUrl = "https://example.com/blog/feed.xml")
            .items
            .single()

    assertEquals(
        """<a href="https://example.com/about">About</a> """ +
            """<a href="https://example.com/blog/next.html">Next</a>""",
        item.contentHtml)
  }

  @Test
  fun `relative links in an Atom body resolve against the xml_base of the content`() {
    val entry =
        parseFeed(
                """
                <feed xmlns="http://www.w3.org/2005/Atom" xml:base="https://base.example.com/blog/">
                  <entry xml:base="2024/">
                    <id>1</id>
                    <content type="html" xml:base="post/">&lt;a href="image.html"&gt;Image&lt;/a&gt;</content>
                  </entry>
                </feed>
                """
                    .trimIndent())
            .items
            .single()

    assertEquals(
        """<a href="https://base.example.com/blog/2024/post/image.html">Image</a>""",
        entry.contentHtml)
  }

  @Test
  fun `links in the body with schemes other than http and https lose their target`() {
    val item =
        parseFeed(
                rssItem(
                    "<content:encoded><![CDATA[" +
                        """<a href="javascript:alert(1)">a</a>""" +
                        """<a href="intent://scan/#Intent;scheme=zxing;end">b</a>""" +
                        """<a href="nostrsigner:{}">c</a>""" +
                        """<a href="mailto:someone@example.com">d</a>""" +
                        """<a href="ftp://example.com/file">e</a>""" +
                        """<a href="https://example.com/ok">f</a>""" +
                        "]]></content:encoded>"))
            .items
            .single()

    assertEquals(
        """<a>a</a><a>b</a><a>c</a><a>d</a><a>e</a><a href="https://example.com/ok">f</a>""",
        item.contentHtml)
  }

  @Test
  fun `item without any body text has no body`() {
    val items =
        parseFeed(
                """
                <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/"><channel>
                  <item><title>None</title></item>
                  <item><title>Blank</title><description>   </description></item>
                  <item><title>Unusable image</title><content:encoded><![CDATA[<img src="javascript:alert(1)">]]></content:encoded></item>
                </channel></rss>
                """
                    .trimIndent())
            .items

    assertEquals(listOf(null, null, null), items.map { it.contentHtml })
  }

  @Test
  fun `Atom entry with content that refers to another document has no body`() {
    val entry =
        parseFeed(
                """
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <entry><id>1</id><content type="text/html" src="https://example.com/a.html"/></entry>
                </feed>
                """
                    .trimIndent())
            .items
            .single()

    assertNull(entry.contentHtml)
  }

  @Test
  fun `stream that fails to read gives a failure instead of throwing`() {
    val failing =
        object : InputStream() {
          override fun read(): Int = throw IOException("connection reset")
        }

    val result = parser.parse(failing, FEED_URL)

    assertTrue(result.isFailure)
  }

  @Test
  fun `HTML page is not a feed`() {
    val result =
        parse(
            """
            <!DOCTYPE html>
            <html><head><title>Page</title><link rel="alternate" href="/feed"></head>
            <body><p>Hello</p></body></html>
            """
                .trimIndent())

    assertIs<UnsupportedFeedFormatException>(result.exceptionOrNull())
  }

  @Test
  fun `text that is not XML is not a feed`() {
    val result = parse("this is not a feed < at all >")

    assertIs<UnsupportedFeedFormatException>(result.exceptionOrNull())
  }

  @Test
  fun `binary garbage is not a feed`() {
    val result = parser.parse(byteArrayOf(0, 1, 2, -1, -2, 65, 66).inputStream(), FEED_URL)

    assertIs<UnsupportedFeedFormatException>(result.exceptionOrNull())
  }

  @Test
  fun `empty body is not a feed`() {
    val result = parse("")

    assertIs<UnsupportedFeedFormatException>(result.exceptionOrNull())
  }

  private fun parse(xml: String, feedUrl: String = FEED_URL): Result<Feed> =
      parser.parse(xml.byteInputStream(), feedUrl)

  private fun parseFeed(xml: String, feedUrl: String = FEED_URL): Feed =
      parse(xml, feedUrl).getOrThrow()

  private fun rssItem(itemBody: String): String =
      """<rss version="2.0"><channel><title>T</title><item>$itemBody</item></channel></rss>"""

  companion object {
    private const val FEED_URL = "https://example.com/feed.xml"

    private val RSS_2_0 =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
          <channel>
            <title>Example Blog</title>
            <atom:link href="https://example.com/feed.xml" rel="self" type="application/rss+xml"/>
            <link>https://example.com/</link>
            <description>Channel description</description>
            <image>
              <title>Logo</title>
              <url>https://example.com/logo.png</url>
              <link>https://example.com/image-link</link>
            </image>
            <item>
              <title>First post</title>
              <link>https://example.com/posts/1</link>
              <guid isPermaLink="false">urn:example:1</guid>
              <pubDate>Tue, 02 Jan 2024 03:04:05 +0900</pubDate>
              <description>&lt;p&gt;Hello &lt;b&gt;world&lt;/b&gt;&lt;/p&gt;</description>
            </item>
            <item>
              <title>Second post</title>
              <link>https://example.com/posts/2</link>
            </item>
          </channel>
        </rss>
        """
            .trimIndent()

    private val RSS_1_0 =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <rdf:RDF
            xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
            xmlns="http://purl.org/rss/1.0/"
            xmlns:dc="http://purl.org/dc/elements/1.1/">
          <channel rdf:about="https://rdf.example.com/rss">
            <title>RDF Blog</title>
            <link>https://rdf.example.com/</link>
            <description>RDF description</description>
            <items>
              <rdf:Seq>
                <rdf:li rdf:resource="https://rdf.example.com/1"/>
                <rdf:li rdf:resource="https://rdf.example.com/2"/>
              </rdf:Seq>
            </items>
          </channel>
          <item rdf:about="https://rdf.example.com/1">
            <title>RDF first</title>
            <link>https://rdf.example.com/1</link>
            <description>RDF summary</description>
            <dc:date>2024-01-02T03:04:05+09:00</dc:date>
          </item>
          <item rdf:about="https://rdf.example.com/2">
            <title>RDF second</title>
            <link>https://rdf.example.com/2</link>
          </item>
        </rdf:RDF>
        """
            .trimIndent()

    private val ATOM =
        """
        <?xml version="1.0" encoding="utf-8"?>
        <feed xmlns="http://www.w3.org/2005/Atom">
          <title>Atom Blog</title>
          <link rel="self" href="https://atom.example.com/feed.atom"/>
          <link rel="alternate" type="text/html" href="https://atom.example.com/"/>
          <id>urn:uuid:feed</id>
          <updated>2024-01-02T03:04:05Z</updated>
          <entry>
            <title>Atom first</title>
            <link rel="edit" href="https://atom.example.com/edit/1"/>
            <link rel="enclosure" href="https://atom.example.com/1.mp3"/>
            <link rel="alternate" href="https://atom.example.com/1"/>
            <id>tag:atom.example.com,2024:1</id>
            <published>2024-01-02T03:04:05Z</published>
            <updated>2024-02-02T03:04:05Z</updated>
            <summary>Atom summary</summary>
            <content type="html">&lt;p&gt;Ignored content&lt;/p&gt;</content>
          </entry>
          <entry>
            <title type="text">Atom second</title>
            <link rel="replies" href="https://atom.example.com/2#comments"/>
            <link href="https://atom.example.com/2"/>
            <id>tag:atom.example.com,2024:2</id>
            <updated>2024-01-02T03:04:05+09:00</updated>
            <content type="html">&lt;p&gt;Body &lt;em&gt;text&lt;/em&gt;&lt;/p&gt;</content>
          </entry>
        </feed>
        """
            .trimIndent()
  }
}
