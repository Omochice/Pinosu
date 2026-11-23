package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

private val GapBeforeHeading = 16.dp
private val GapBetweenBlocks = 8.dp

private data class RenderedBlock(val text: AnnotatedString, val isHeading: Boolean)

/**
 * Text rendered from HTML as spaced blocks, with tappable links
 *
 * @param html HTML to render, already reduced to text formatting
 * @param onOpenLink Callback with the URL of a link that is tapped
 * @param modifier Modifier for the blocks
 * @param style Style of the text outside headings
 * @param headingStyle Style of headings, which are additionally enlarged and bolded by the HTML
 */
@Composable
fun HtmlText(
    html: String,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    headingStyle: TextStyle = style.copy(lineHeight = TextUnit.Unspecified),
) {
  val linkColor = MaterialTheme.colorScheme.primary
  val currentOnOpenLink by rememberUpdatedState(onOpenLink)
  val blocks =
      remember(html, linkColor) {
        val linkStyles =
            TextLinkStyles(
                style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
        val linkListener = LinkInteractionListener { link ->
          (link as? LinkAnnotation.Url)?.let { currentOnOpenLink(it.url) }
        }
        splitHtmlBlocks(html).map { block ->
          val text =
              AnnotatedString.fromHtml(
                  htmlString = block.html,
                  linkStyles = linkStyles,
                  linkInteractionListener = linkListener)
          // fromHtml ends a block element with a line break, which would add a blank line on
          // top of the gap between blocks.
          RenderedBlock(text.subSequence(0, text.text.trimEnd().length), block.isHeading)
        }
      }
  Column(modifier = modifier) {
    blocks.forEachIndexed { index, block ->
      if (index > 0) {
        Spacer(
            modifier = Modifier.height(if (block.isHeading) GapBeforeHeading else GapBetweenBlocks))
      }
      Text(text = block.text, style = if (block.isHeading) headingStyle else style)
    }
  }
}
