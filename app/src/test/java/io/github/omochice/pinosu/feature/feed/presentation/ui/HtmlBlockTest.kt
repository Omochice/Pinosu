package io.github.omochice.pinosu.feature.feed.presentation.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlBlockTest {

  @Test
  fun `a heading becomes its own block apart from the paragraphs around it`() {
    val blocks = splitHtmlBlocks("<p>Intro</p><h2>Section</h2><p>Body</p>")

    assertEquals(
        listOf(
            TextBlock("<p>Intro</p>", isHeading = false),
            TextBlock("<h2>Section</h2>", isHeading = true),
            TextBlock("<p>Body</p>", isHeading = false)),
        blocks)
  }

  @Test
  fun `every heading level is a heading block`() {
    val blocks = splitHtmlBlocks((1..6).joinToString("") { "<h$it>Level $it</h$it>" })

    assertEquals(List(6) { true }, blocks.map { (it as TextBlock).isHeading })
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
        blocks.map { (it as TextBlock).html })
  }

  @Test
  fun `links keep their target inside a block`() {
    val blocks =
        splitHtmlBlocks("<h3>See <a href=\"https://blog.example/a\">this</a></h3><p>Text</p>")

    assertEquals(
        "<h3>See <a href=\"https://blog.example/a\">this</a></h3>",
        (blocks.first() as TextBlock).html)
  }

  @Test
  fun `inline content outside any block is grouped into one block`() {
    val blocks =
        splitHtmlBlocks(
            "Lead <a href=\"https://blog.example/a\">link</a> text<br>more<h2>Next</h2>tail")

    assertEquals(
        listOf(
            TextBlock("Lead <a href=\"https://blog.example/a\">link</a> text<br>more", false),
            TextBlock("<h2>Next</h2>", true),
            TextBlock("tail", false)),
        blocks)
  }

  @Test
  fun `a div wrapping blocks is opened so that its headings are split`() {
    val blocks = splitHtmlBlocks("<div><h1>Title</h1><div><p>Nested</p></div></div>")

    assertEquals(
        listOf(TextBlock("<h1>Title</h1>", true), TextBlock("<p>Nested</p>", false)), blocks)
  }

  @Test
  fun `a div holding only inline content is a single block`() {
    val blocks = splitHtmlBlocks("<div>Just <b>text</b></div>")

    assertEquals(listOf(TextBlock("<div>Just <b>text</b></div>", false)), blocks)
  }

  @Test
  fun `blocks without text are dropped`() {
    val blocks = splitHtmlBlocks("<p> </p>\n  <h2></h2><p>Kept</p><br>  ")

    assertEquals(listOf(TextBlock("<p>Kept</p>", false)), blocks)
  }

  @Test
  fun `a table becomes a table block with the inline HTML and header flag of each cell`() {
    val blocks =
        splitHtmlBlocks(
            "<table><tr><th>Name</th><th>Site</th></tr>" +
                "<tr><td><b>Alice</b></td><td><a href=\"https://blog.example/a\">Blog</a></td></tr>" +
                "</table>")

    assertEquals(
        listOf(
            TableBlock(
                Table(
                    caption = null,
                    rows =
                        listOf(
                            listOf(TableCell("Name", true), TableCell("Site", true)),
                            listOf(
                                TableCell("<b>Alice</b>", false),
                                TableCell(
                                    "<a href=\"https://blog.example/a\">Blog</a>", false)))))),
        blocks)
  }

  @Test
  fun `a cell spanning columns keeps its span`() {
    val table =
        tableOf("<table><tr><th colspan=\"2\">Both</th></tr><tr><td>A</td><td>B</td></tr></table>")

    assertEquals(
        listOf(
            listOf(TableCell("Both", true, columnSpan = 2)),
            listOf(TableCell("A", false), TableCell("B", false))),
        table.rows)
  }

  @Test
  fun `a cell spanning rows leaves an empty cell in its column of the rows below`() {
    val table =
        tableOf(
            """<table><tr><th rowspan="2" colspan="2">Group</th><td>A</td></tr>""" +
                "<tr><td>B</td></tr><tr><td>C</td><td>D</td><td>E</td></tr></table>")

    assertEquals(
        listOf(
            listOf(TableCell("Group", true, columnSpan = 2), TableCell("A", false)),
            listOf(TableCell("", true, columnSpan = 2), TableCell("B", false)),
            listOf(TableCell("C", false), TableCell("D", false), TableCell("E", false))),
        table.rows)
  }

  @Test
  fun `rows shorter than the widest row are padded with empty cells`() {
    val table = tableOf("<table><tr><td>A</td><td>B</td><td>C</td></tr><tr><td>D</td></tr></table>")

    assertEquals(
        listOf(
            listOf(TableCell("A", false), TableCell("B", false), TableCell("C", false)),
            listOf(TableCell("D", false), TableCell("", false), TableCell("", false))),
        table.rows)
  }

  @Test
  fun `header body and footer rows are read in document order`() {
    val table =
        tableOf(
            "<table><thead><tr><th>Head</th><th>H</th></tr></thead>" +
                "<tbody><tr><td>Body</td><td>B</td></tr></tbody>" +
                "<tfoot><tr><td>Foot</td><td>F</td></tr></tfoot></table>")

    assertEquals(listOf("Head", "Body", "Foot"), table.rows.map { it.first().content })
  }

  @Test
  fun `the caption of a table is read apart from its rows`() {
    val table =
        tableOf(
            "<table><caption>Scores <i>2026</i></caption>" +
                "<tr><td>A</td><td>B</td></tr></table>")

    assertEquals("Scores <i>2026</i>", table.caption)
    assertEquals(listOf(listOf(TableCell("A", false), TableCell("B", false))), table.rows)
  }

  @Test
  fun `a table nested in a cell is reduced to its text`() {
    val table =
        tableOf(
            "<table><tr><td>Outer <table><tr><td>A</td><td>B</td></tr></table></td>" +
                "<td>Next</td></tr></table>")

    assertEquals(
        listOf(listOf(TableCell("Outer A B", false), TableCell("Next", false))), table.rows)
  }

  @Test
  fun `a table wrapped in a div is split out from the blocks beside it`() {
    val blocks =
        splitHtmlBlocks("<div><p>Before</p><table><tr><td>A</td><td>B</td></tr></table>tail</div>")

    assertEquals(
        listOf(
            TextBlock("<p>Before</p>", false),
            TableBlock(
                Table(
                    caption = null,
                    rows = listOf(listOf(TableCell("A", false), TableCell("B", false))))),
            TextBlock("tail", false)),
        blocks)
  }

  @Test
  fun `blocks before and after a table keep their order`() {
    val blocks =
        splitHtmlBlocks(
            "<p>Before</p><table><tr><td>A</td><td>B</td></tr></table><h2>After</h2><p>End</p>")

    assertEquals(
        listOf(TextBlock::class, TableBlock::class, TextBlock::class, TextBlock::class),
        blocks.map { it::class })
    assertEquals(TextBlock("<h2>After</h2>", true), blocks[2])
  }

  @Test
  fun `a table holding a single cell is opened like a div wrapping blocks`() {
    val blocks =
        splitHtmlBlocks("<table><tr><td><h2>Title</h2><p>Laid out by a table</p></td></tr></table>")

    assertEquals(
        listOf(TextBlock("<h2>Title</h2>", true), TextBlock("<p>Laid out by a table</p>", false)),
        blocks)
  }

  @Test
  fun `a table without text is dropped`() {
    val blocks = splitHtmlBlocks("<table><tr><td> </td><td></td></tr></table><p>Kept</p>")

    assertEquals(listOf(TextBlock("<p>Kept</p>", false)), blocks)
  }

  private fun tableOf(html: String): Table<String> =
      (splitHtmlBlocks(html).single() as TableBlock).table
}
