package io.github.omochice.pinosu.feature.feed.presentation.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlBlockTest {

  @Test
  fun `a heading becomes its own block apart from the paragraphs around it`() {
    val blocks = splitHtmlBlocks("<p>Intro</p><h2>Section</h2><p>Body</p>")

    assertEquals(
        listOf(
            HtmlBlock("<p>Intro</p>", isHeading = false),
            HtmlBlock("<h2>Section</h2>", isHeading = true),
            HtmlBlock("<p>Body</p>", isHeading = false)),
        blocks)
  }

  @Test
  fun `every heading level is a heading block`() {
    val blocks = splitHtmlBlocks((1..6).joinToString("") { "<h$it>Level $it</h$it>" })

    assertEquals(List(6) { true }, blocks.map { it.isHeading })
  }

  @Test
  fun `paragraphs lists quotes and preformatted text stay separate blocks in order`() {
    val blocks =
        splitHtmlBlocks(
            "<p>One</p><ul><li>A</li><li>B</li></ul><blockquote>Quote</blockquote>" +
                "<pre>code  here</pre><ol><li>C</li></ol>")

    assertEquals(
        listOf(
            "<p>One</p>",
            "<ul><li>A</li><li>B</li></ul>",
            "<blockquote>Quote</blockquote>",
            "<pre>code  here</pre>",
            "<ol><li>C</li></ol>"),
        blocks.map { it.html })
  }

  @Test
  fun `links keep their target inside a block`() {
    val blocks =
        splitHtmlBlocks("<h3>See <a href=\"https://blog.example/a\">this</a></h3><p>Text</p>")

    assertEquals("<h3>See <a href=\"https://blog.example/a\">this</a></h3>", blocks.first().html)
  }

  @Test
  fun `inline content outside any block is grouped into one block`() {
    val blocks =
        splitHtmlBlocks(
            "Lead <a href=\"https://blog.example/a\">link</a> text<br>more<h2>Next</h2>tail")

    assertEquals(
        listOf(
            HtmlBlock("Lead <a href=\"https://blog.example/a\">link</a> text<br>more", false),
            HtmlBlock("<h2>Next</h2>", true),
            HtmlBlock("tail", false)),
        blocks)
  }

  @Test
  fun `a div wrapping blocks is opened so that its headings are split`() {
    val blocks = splitHtmlBlocks("<div><h1>Title</h1><div><p>Nested</p></div></div>")

    assertEquals(
        listOf(HtmlBlock("<h1>Title</h1>", true), HtmlBlock("<p>Nested</p>", false)), blocks)
  }

  @Test
  fun `a div holding only inline content is a single block`() {
    val blocks = splitHtmlBlocks("<div>Just <b>text</b></div>")

    assertEquals(listOf(HtmlBlock("<div>Just <b>text</b></div>", false)), blocks)
  }

  @Test
  fun `blocks without text are dropped`() {
    val blocks = splitHtmlBlocks("<p> </p>\n  <h2></h2><p>Kept</p><br>  ")

    assertEquals(listOf(HtmlBlock("<p>Kept</p>", false)), blocks)
  }
}
