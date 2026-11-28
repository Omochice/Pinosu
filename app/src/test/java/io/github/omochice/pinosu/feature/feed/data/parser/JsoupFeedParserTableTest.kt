package io.github.omochice.pinosu.feature.feed.data.parser

import kotlin.test.Test
import kotlin.test.assertEquals

class JsoupFeedParserTableTest {

  private val parser = JsoupFeedParser()

  @Test
  fun `body keeps tables with their caption and row groups`() {
    val html =
        "<table><caption>Scores</caption><thead><tr><th>Name</th><th>Score</th></tr></thead>" +
            "<tbody><tr><td>Alice</td><td>90</td></tr></tbody>" +
            "<tfoot><tr><td>Total</td><td>90</td></tr></tfoot></table>"

    assertEquals(html, bodyOf(html))
  }

  @Test
  fun `body keeps the colspan and rowspan of table cells`() {
    val body =
        bodyOf(
            """<table><tr><th colspan="2">Both</th></tr>""" +
                """<tr><td rowspan="2">Tall</td><td>A</td></tr><tr><td>B</td></tr></table>""")

    assertEquals(
        """<table><tbody><tr><th colspan="2">Both</th></tr>""" +
            """<tr><td rowspan="2">Tall</td><td>A</td></tr><tr><td>B</td></tr></tbody></table>""",
        body)
  }

  @Test
  fun `body drops colspan and rowspan values that are not small positive integers`() {
    val body =
        bodyOf(
            """<table><tr><td colspan="0">a</td><td rowspan="-1">b</td><td colspan="x">c</td>""" +
                """<td rowspan="2.5">d</td><td colspan="999">e</td><td rowspan="">f</td>""" +
                """<td colspan=" 3 ">g</td></tr></table>""")

    assertEquals(
        "<table><tbody><tr><td>a</td><td>b</td><td>c</td><td>d</td><td>e</td><td>f</td>" +
            """<td colspan="3">g</td></tr></tbody></table>""",
        body)
  }

  @Test
  fun `body drops other attributes of tables and their cells`() {
    val body =
        bodyOf(
            """<table border="1" style="width:100%" colspan="2"><tr class="row">""" +
                """<th scope="col" width="50" onclick="steal()">H</th>""" +
                """<td style="color:red" align="center" bgcolor="red">D</td></tr></table>""")

    assertEquals("<table><tbody><tr><th>H</th><td>D</td></tr></tbody></table>", body)
  }

  @Test
  fun `body keeps images inside table cells`() {
    val body =
        bodyOf("""<table><tr><td>Logo<img src="https://example.com/a.png"></td></tr></table>""")

    assertEquals(
        """<table><tbody><tr><td>Logo<img src="https://example.com/a.png"></td></tr></tbody></table>""",
        body)
  }

  @Test
  fun `links inside table cells resolve against the feed url and keep only http targets`() {
    val body =
        bodyOf(
            """<table><tr><td><a href="/about">About</a></td>""" +
                """<td><a href="javascript:alert(1)">Bad</a></td></tr></table>""")

    assertEquals(
        """<table><tbody><tr><td><a href="https://example.com/about">About</a></td>""" +
            "<td><a>Bad</a></td></tr></tbody></table>",
        body)
  }

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
