package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration

/**
 * Text rendered from HTML, with tappable links
 *
 * @param html HTML to render, already reduced to text formatting
 * @param onOpenLink Callback with the URL of a link that is tapped
 * @param modifier Modifier for the text
 */
@Composable
fun HtmlText(html: String, onOpenLink: (String) -> Unit, modifier: Modifier = Modifier) {
  val linkColor = MaterialTheme.colorScheme.primary
  val currentOnOpenLink by rememberUpdatedState(onOpenLink)
  val text =
      remember(html, linkColor) {
        AnnotatedString.fromHtml(
            htmlString = html,
            linkStyles =
                TextLinkStyles(
                    style =
                        SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)),
            linkInteractionListener = { link ->
              (link as? LinkAnnotation.Url)?.let { currentOnOpenLink(it.url) }
            })
      }
  Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = modifier)
}
