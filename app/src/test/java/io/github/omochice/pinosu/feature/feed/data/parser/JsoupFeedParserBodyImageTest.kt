package io.github.omochice.pinosu.feature.feed.data.parser

import kotlin.test.Test
import kotlin.test.assertEquals

class JsoupFeedParserBodyImageTest {

  private val parser = JsoupFeedParser()

  @Test
  fun `body keeps images with only their source resolved against the feed url and their alt`() {
    val body =
        bodyOf(
            """<p>Hi</p><img src="/a.png" alt="A" width="10" height="20" align="left" """ +
                """title="T" onerror="steal()">""")

    assertEquals("""<p>Hi</p><img src="https://example.com/a.png" alt="A">""", body)
  }

  @Test
  fun `body removes images whose source is missing or does not resolve to an http url`() {
    val body =
        bodyOf(
            """<p>Hi</p><img src="javascript:alert(1)"><img src="data:image/png;base64,AAAA">""" +
                """<img><img src=" "><img data-src="https://example.com/lazy.png">""")

    assertEquals("<p>Hi</p>", body)
  }

  @Test
  fun `body holding only an image is kept`() {
    assertEquals("""<img src="https://example.com/a.png">""", bodyOf("""<img src="a.png">"""))
  }

  @Test
  fun `body keeps an image inside a link`() {
    val body = bodyOf("""<a href="/post"><img src="/a.png" alt="A"></a>""")

    assertEquals(
        """<a href="https://example.com/post"><img src="https://example.com/a.png" alt="A"></a>""",
        body)
  }

  @Test
  fun `RSS body prefers a description with text over content encoded holding only an image`() {
    val body =
        contentHtmlOf(
            """<rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/">""" +
                "<channel><title>T</title><item>" +
                """<content:encoded><![CDATA[<img src="a.png">]]></content:encoded>""" +
                "<description>Short</description></item></channel></rss>")

    assertEquals("Short", body)
  }

  @Test
  fun `Atom body prefers a summary with text over content holding only an image`() {
    val body =
        contentHtmlOf(
            """<feed xmlns="http://www.w3.org/2005/Atom"><entry><id>1</id>""" +
                """<content type="html">&lt;img src="a.png"&gt;</content>""" +
                "<summary>Short</summary></entry></feed>")

    assertEquals("Short", body)
  }

  private fun contentHtmlOf(feed: String): String? =
      parser.parse(feed.byteInputStream(), FEED_URL).getOrThrow().items.single().contentHtml

  private fun bodyOf(html: String): String? =
      parser
          .parse(
              ("""<rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/">""" +
                      "<channel><title>T</title><item>" +
                      "<content:encoded><![CDATA[$html]]></content:encoded>" +
                      "</item></channel></rss>")
                  .byteInputStream(),
              FEED_URL)
          .getOrThrow()
          .items
          .single()
          .contentHtml

  companion object {
    private const val FEED_URL = "https://example.com/feed.xml"
  }
}
