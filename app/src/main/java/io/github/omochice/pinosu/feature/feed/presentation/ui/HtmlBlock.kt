package io.github.omochice.pinosu.feature.feed.presentation.ui

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * One block of an HTML body, laid out apart from its neighbours
 *
 * @param T Type of the content of its text or table cells, such as HTML or styled text
 */
internal sealed interface HtmlBlock<out T> {

  /** Block at the same place whose text or table cells hold [transform] of their content */
  fun <R> map(transform: (T) -> R): HtmlBlock<R>
}

/** Block of an HTML body that is laid out as text */
internal data class TextBlock<out T>(val content: T, val isHeading: Boolean) : HtmlBlock<T> {
  override fun <R> map(transform: (T) -> R): TextBlock<R> = TextBlock(transform(content), isHeading)
}

/** Block of an HTML body that is laid out as a grid of cells */
internal data class TableBlock<T>(val table: Table<T>) : HtmlBlock<T> {
  override fun <R> map(transform: (T) -> R): TableBlock<R> = TableBlock(table.map(transform))
}

/**
 * Block of an HTML body that is laid out as an image
 *
 * @property url Absolute HTTP(S) URL of the image
 * @property alt Text describing the image, empty when it has none
 */
internal data class ImageBlock(val url: String, val alt: String) : HtmlBlock<Nothing> {
  override fun <R> map(transform: (Nothing) -> R): ImageBlock = this
}

private const val DIV = "div"
private const val TABLE = "table"
private const val IMAGE = "img"
private val HEADING_TAGS = setOf("h1", "h2", "h3", "h4", "h5", "h6")
private val BLOCK_TAGS =
    HEADING_TAGS + setOf("p", "ul", "ol", "dl", "blockquote", "pre", DIV, TABLE)

/**
 * Split HTML into its block-level parts
 *
 * An image outside a table cell becomes a block of its own, which splits the text around it out of
 * the element that held both, at the cost of that element, such as a heading or a link.
 *
 * @param html HTML to split
 * @return The blocks with text or an image, in document order
 */
internal fun splitHtmlBlocks(html: String): List<HtmlBlock<String>> {
  val document = Jsoup.parseBodyFragment(html)
  document.outputSettings().prettyPrint(false)
  return buildList { collectBlocks(document.body(), this) }
}

private fun collectBlocks(parent: Element, blocks: MutableList<HtmlBlock<String>>) {
  val inlineRun = mutableListOf<Node>()
  fun flushInlineRun() {
    if (inlineRun.any { it.hasVisibleText() }) {
      blocks += TextBlock(inlineRun.joinToString("") { it.outerHtml() }, isHeading = false)
    }
    inlineRun.clear()
  }
  parent.childNodes().forEach { node ->
    val holdsImage = node is Element && node.selectFirst(IMAGE) != null
    if (node is Element && (node.normalName() in BLOCK_TAGS || holdsImage)) {
      flushInlineRun()
      addBlock(node, holdsImage, blocks)
    } else {
      inlineRun += node
    }
  }
  flushInlineRun()
}

private fun addBlock(
    element: Element,
    holdsImage: Boolean,
    blocks: MutableList<HtmlBlock<String>>
) {
  when {
    element.normalName() == IMAGE -> blocks += ImageBlock(element.attr("src"), element.attr("alt"))
    element.normalName() == TABLE -> addTable(element, blocks)
    holdsImage -> collectBlocks(element, blocks)
    element.normalName() == DIV && element.children().any { it.normalName() in BLOCK_TAGS } ->
        collectBlocks(element, blocks)
    element.hasText() ->
        blocks += TextBlock(element.outerHtml(), isHeading = element.normalName() in HEADING_TAGS)
  }
}

/** A table with a single cell is opened like a `div`, because it lays out the page around it. */
private fun addTable(table: Element, blocks: MutableList<HtmlBlock<String>>) {
  val soleCell = soleCellOf(table)
  when {
    soleCell != null -> collectBlocks(soleCell, blocks)
    table.hasText() -> blocks += TableBlock(parseHtmlTable(table))
  }
}

private fun Node.hasVisibleText(): Boolean =
    when (this) {
      is TextNode -> !isBlank
      is Element -> hasText()
      else -> false
    }
