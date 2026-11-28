package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

private val GapBeforeHeading = 28.dp
private val GapBetweenBlocks = 16.dp

/**
 * Text and images rendered from HTML as spaced blocks, with tappable links
 *
 * @param html HTML to render, already reduced to text formatting, tables, and images
 * @param onOpenLink Callback with the URL of a link that is tapped
 * @param modifier Modifier for the blocks
 * @param style Style of the text outside headings, including table captions and cells
 * @param headingStyle Style of headings, which are additionally enlarged and bolded by the HTML
 * @param omittedImageUrl URL of an image already shown next to the blocks, left out of them so that
 *   it is not shown twice unless nothing else would be left, or null to leave out none
 */
@Composable
fun HtmlText(
    html: String,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    headingStyle: TextStyle = style.copy(lineHeight = TextUnit.Unspecified),
    omittedImageUrl: String? = null,
) {
  val linkColor = MaterialTheme.colorScheme.primary
  val currentOnOpenLink by rememberUpdatedState(onOpenLink)
  val blocks =
      remember(html, linkColor, omittedImageUrl) {
        val linkStyles =
            TextLinkStyles(
                style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
        val linkListener = LinkInteractionListener { link ->
          (link as? LinkAnnotation.Url)?.let { currentOnOpenLink(it.url) }
        }
        val rendered = renderHtmlBlocks(html, linkStyles, linkListener)
        rendered.filterNot { it is RenderedImage && it.url == omittedImageUrl }.ifEmpty { rendered }
      }
  Column(modifier = modifier) {
    blocks.forEachIndexed { index, block ->
      if (index > 0) {
        Spacer(modifier = Modifier.height(gapAbove(block)))
      }
      when (block) {
        is RenderedText ->
            Text(text = block.text, style = if (block.isHeading) headingStyle else style)
        is RenderedTable -> ArticleTable(table = block.table, style = style)
        is RenderedImage -> ArticleImage(image = block)
      }
    }
  }
}

@Composable
private fun ArticleImage(image: RenderedImage) {
  // The height follows from the width by the image's own ratio rather than from the image
  // loader's measuring, which kept the height of a previous width after the screen rotated.
  var aspectRatio by remember(image.url) { mutableStateOf<Float?>(null) }
  val ratio = aspectRatio
  AsyncImage(
      model = image.url,
      contentDescription = image.alt.ifBlank { null },
      modifier =
          Modifier.fillMaxWidth()
              .then(if (ratio == null) Modifier else Modifier.aspectRatio(ratio))
              .clip(RoundedCornerShape(8.dp)),
      alignment = Alignment.CenterStart,
      contentScale = if (ratio == null) ContentScale.Inside else ContentScale.Fit,
      onSuccess = { aspectRatio = articleImageAspectRatio(it.painter.intrinsicSize) })
}

/**
 * Aspect ratio at which an image in an article body fills the width of the article
 *
 * Icons and tracking pixels keep their own size, because stretched to the width they would be a
 * blur or a large empty square.
 *
 * @param intrinsicSize Size of the decoded image in pixels
 * @return Width divided by height for a picture, or null for anything that keeps its own size
 */
internal fun articleImageAspectRatio(intrinsicSize: Size): Float? =
    intrinsicSize
        .takeIf { it.isSpecified && it.width >= MIN_FILL_WIDTH_PX && it.height > 0f }
        ?.let { it.width / it.height }

private const val MIN_FILL_WIDTH_PX = 64f

private fun gapAbove(block: RenderedBlock): Dp =
    if (block is RenderedText && block.isHeading) GapBeforeHeading else GapBetweenBlocks
