package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml

/** One block of an HTML body with its HTML converted to styled text */
internal sealed interface RenderedBlock

/**
 * Block laid out as one piece of text
 *
 * @property text Styled text of the block
 * @property isHeading Whether the block is a heading
 */
internal data class RenderedText(val text: AnnotatedString, val isHeading: Boolean) : RenderedBlock

/**
 * Split HTML into blocks and convert each to styled text with tappable links
 *
 * @param html HTML to render, already reduced to text formatting
 * @param linkStyles Styles of the links in the text
 * @param linkListener Listener called when a link in the text is tapped
 * @return The blocks with text, in document order
 */
internal fun renderHtmlBlocks(
    html: String,
    linkStyles: TextLinkStyles,
    linkListener: LinkInteractionListener,
): List<RenderedBlock> {
  fun annotate(blockHtml: String): AnnotatedString {
    val text =
        AnnotatedString.fromHtml(
            htmlString = blockHtml, linkStyles = linkStyles, linkInteractionListener = linkListener)
    // fromHtml ends a block element with a line break, which would add a blank line on top of the
    // gap between blocks.
    return text.subSequence(0, text.text.trimEnd().length)
  }
  return splitHtmlBlocks(html).map { block ->
    when (block) {
      is TextBlock -> RenderedText(annotate(block.html), block.isHeading)
    }
  }
}
