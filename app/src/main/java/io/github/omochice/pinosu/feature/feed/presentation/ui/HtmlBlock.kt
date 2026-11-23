package io.github.omochice.pinosu.feature.feed.presentation.ui

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * One block of an HTML body, laid out apart from its neighbours
 *
 * @property html HTML of the block
 * @property isHeading Whether the block is a heading
 */
internal data class HtmlBlock(val html: String, val isHeading: Boolean)

private const val DIV = "div"
private val HEADING_TAGS = setOf("h1", "h2", "h3", "h4", "h5", "h6")
private val BLOCK_TAGS = HEADING_TAGS + setOf("p", "ul", "ol", "dl", "blockquote", "pre", DIV)

/**
 * Split HTML into its block-level parts
 *
 * @param html HTML to split
 * @return The blocks with text, in document order
 */
internal fun splitHtmlBlocks(html: String): List<HtmlBlock> {
  val document = Jsoup.parseBodyFragment(html)
  document.outputSettings().prettyPrint(false)
  return buildList { collectBlocks(document.body(), this) }
}

private fun collectBlocks(parent: Element, blocks: MutableList<HtmlBlock>) {
  val inlineRun = mutableListOf<Node>()
  fun flushInlineRun() {
    if (inlineRun.any { it.hasVisibleText() }) {
      blocks += HtmlBlock(inlineRun.joinToString("") { it.outerHtml() }, isHeading = false)
    }
    inlineRun.clear()
  }
  parent.childNodes().forEach { node ->
    if (node is Element && node.normalName() in BLOCK_TAGS) {
      flushInlineRun()
      addBlock(node, blocks)
    } else {
      inlineRun += node
    }
  }
  flushInlineRun()
}

private fun addBlock(element: Element, blocks: MutableList<HtmlBlock>) {
  when {
    element.normalName() == DIV && element.children().any { it.normalName() in BLOCK_TAGS } ->
        collectBlocks(element, blocks)
    element.hasText() ->
        blocks += HtmlBlock(element.outerHtml(), isHeading = element.normalName() in HEADING_TAGS)
  }
}

private fun Node.hasVisibleText(): Boolean =
    when (this) {
      is TextNode -> !isBlank
      is Element -> hasText()
      else -> false
    }
