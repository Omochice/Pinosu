package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import java.time.Instant
import kotlinx.coroutines.launch

// Phrase breaking is applied by the platform from API 33 only.
// Compose skips the line break config below it, so older devices keep the default word breaking
// instead of failing, which is why no version check is needed here.
private val PhraseParagraphBreak = LineBreak.Paragraph.copy(wordBreak = LineBreak.WordBreak.Phrase)
private val PhraseHeadingBreak = LineBreak.Heading.copy(wordBreak = LineBreak.WordBreak.Phrase)

/**
 * Composable function for the screen reading one feed item in the app
 *
 * @param item Item to read, or null when it is no longer loaded
 * @param onNavigateUp Callback when the user leaves the reader
 * @param onBookmark Callback with the link and title of the item when its bookmark action is tapped
 * @param isReadOnly Whether to hide the bookmark action for read-only login
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedItemReaderScreen(
    item: FeedItem?,
    onNavigateUp: () -> Unit,
    onBookmark: (link: String, title: String) -> Unit = { _, _ -> },
    isReadOnly: Boolean = false,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val openLink = rememberLinkOpener(snackbarHostState)
  val bookmark: ((String) -> Unit)? =
      if (isReadOnly || item == null) null else { link -> onBookmark(link, item.title) }

  Scaffold(
      topBar = {
        FeedItemReaderTopBar(
            link = item?.link,
            onNavigateUp = onNavigateUp,
            onOpenInBrowser = openLink,
            onBookmark = bookmark)
      },
      snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        val modifier = Modifier.padding(paddingValues).fillMaxSize()
        if (item == null) {
          FeedItemNotFound(onNavigateUp = onNavigateUp, modifier = modifier)
        } else {
          FeedItemArticle(item = item, onOpenLink = openLink, modifier = modifier)
        }
      }
}

@Composable
private fun rememberLinkOpener(snackbarHostState: SnackbarHostState): (String) -> Unit {
  val uriHandler = LocalUriHandler.current
  val scope = rememberCoroutineScope()
  val failedMessage = stringResource(R.string.error_url_open_failed)
  return remember(uriHandler, scope, failedMessage, snackbarHostState) {
    { url ->
      if (!openWebLink(uriHandler, url)) {
        scope.launch { snackbarHostState.showSnackbar(failedMessage) }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedItemReaderTopBar(
    link: String?,
    onNavigateUp: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
    onBookmark: ((String) -> Unit)?,
) {
  TopAppBar(
      title = {},
      navigationIcon = {
        IconButton(onClick = onNavigateUp) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.cd_navigate_up))
        }
      },
      actions = {
        if (link != null) {
          if (onBookmark != null) {
            IconButton(onClick = { onBookmark(link) }) {
              Icon(
                  imageVector = Icons.Outlined.BookmarkAdd,
                  contentDescription = stringResource(R.string.cd_bookmark_feed_item))
            }
          }
          IconButton(onClick = { onOpenInBrowser(link) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = stringResource(R.string.button_open_in_browser))
          }
        }
      })
}

@Composable
private fun FeedItemArticle(item: FeedItem, onOpenLink: (String) -> Unit, modifier: Modifier) {
  Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
    Text(
        text = item.title.ifEmpty { item.link.orEmpty() },
        style = MaterialTheme.typography.headlineSmall.copy(lineBreak = PhraseHeadingBreak))
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = feedItemByline(item),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(16.dp))
    FeedItemBody(item = item, onOpenLink = onOpenLink)
  }
}

@Composable
private fun FeedItemBody(item: FeedItem, onOpenLink: (String) -> Unit) {
  val html = item.contentHtml
  val bodyStyle = MaterialTheme.typography.bodyLarge.copy(lineBreak = PhraseParagraphBreak)
  when {
    html != null ->
        HtmlText(
            html = html,
            onOpenLink = onOpenLink,
            style = bodyStyle,
            headingStyle =
                bodyStyle.copy(lineHeight = TextUnit.Unspecified, lineBreak = PhraseHeadingBreak))
    item.summary.isNotBlank() -> Text(text = item.summary, style = bodyStyle)
    else ->
        Text(
            text = stringResource(R.string.message_feed_item_no_content),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun FeedItemNotFound(onNavigateUp: () -> Unit, modifier: Modifier) {
  Column(
      modifier = modifier.padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.message_feed_item_not_found),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateUp) { Text(stringResource(R.string.button_back_to_feeds)) }
      }
}

@Preview(showBackground = true)
@Composable
private fun FeedItemReaderScreenPreview() {
  FeedItemReaderScreen(
      item =
          FeedItem(
              id = "1",
              feedUrl = "https://blog.example/feed",
              feedTitle = "Example Blog",
              title = "Reading feeds on Nostr",
              link = "https://blog.example/feeds",
              publishedAt = Instant.parse("2026-01-02T03:04:05Z"),
              summary = "A short summary of the article.",
              contentHtml =
                  "<h2>Introduction</h2><p>The whole article, with <b>bold</b> text and a " +
                      "<a href=\"https://blog.example/\">link</a>.</p><ul><li>One</li><li>Two</li></ul>"),
      onNavigateUp = {})
}

@Preview(showBackground = true)
@Composable
private fun ReaderNotFoundPreview() {
  FeedItemReaderScreen(item = null, onNavigateUp = {})
}
