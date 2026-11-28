package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import io.github.omochice.pinosu.ui.component.CardTitle
import io.github.omochice.pinosu.ui.component.OgpThumbnail
import io.github.omochice.pinosu.ui.component.ThumbnailRow
import java.time.Instant

/**
 * Card showing one feed item, laid out like a bookmark in the bookmark list
 *
 * @param item Item to show
 * @param imageUrl Image to show beside the item, or null to show the placeholder
 * @param onOpen Callback when the card is tapped
 * @param onBookmark Callback when the bookmark action is tapped, or null to hide the action
 */
@Composable
fun FeedItemCard(
    item: FeedItem,
    imageUrl: String?,
    onOpen: (FeedItem) -> Unit,
    onBookmark: (() -> Unit)?,
) {
  val title = feedItemTitle(item)
  FeedItemCardShell(onClick = { onOpen(item) }) {
    ThumbnailRow(imageUrl = imageUrl, contentDescription = title) {
      FeedItemText(title = title, byline = feedItemByline(item), modifier = Modifier.weight(1f))
      onBookmark?.let { FeedItemBookmarkButton(onClick = it) }
    }
  }
}

/**
 * Card showing one feed item in the timeline grid, laid out like a bookmark in the bookmark grid
 *
 * The bookmark action sits below the text rather than beside it, because a grid column is too
 * narrow to give the title room next to the button.
 *
 * @param item Item to show
 * @param imageUrl Image to show above the item, or null to show the placeholder
 * @param onOpen Callback when the card is tapped
 * @param onBookmark Callback when the bookmark action is tapped, or null to hide the action
 */
@Composable
fun FeedItemGridCard(
    item: FeedItem,
    imageUrl: String?,
    onOpen: (FeedItem) -> Unit,
    onBookmark: (() -> Unit)?,
) {
  val title = feedItemTitle(item)
  FeedItemCardShell(onClick = { onOpen(item) }) {
    Column {
      OgpThumbnail(
          imageUrl = imageUrl,
          contentDescription = title,
          modifier = Modifier.fillMaxWidth().height(100.dp))
      Column(modifier = Modifier.padding(12.dp)) {
        FeedItemText(title = title, byline = feedItemByline(item))
        onBookmark?.let {
          Box(modifier = Modifier.align(Alignment.End)) { FeedItemBookmarkButton(onClick = it) }
        }
      }
    }
  }
}

@Composable
private fun FeedItemCardShell(onClick: () -> Unit, content: @Composable () -> Unit) {
  Card(
      modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        content()
      }
}

@Composable
private fun FeedItemText(title: String, byline: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    CardTitle(text = title)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = byline,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis)
  }
}

@Preview(showBackground = true)
@Composable
private fun FeedItemCardPreview() {
  FeedItemCard(
      item =
          FeedItem(
              id = "1",
              feedUrl = "https://blog.example/feed",
              feedTitle = "Example Blog",
              title = "Reading feeds on Nostr",
              link = "https://blog.example/feeds",
              publishedAt = Instant.parse("2026-01-02T03:04:05Z"),
              summary = "A short summary of the article."),
      imageUrl = null,
      onOpen = {},
      onBookmark = {})
}
