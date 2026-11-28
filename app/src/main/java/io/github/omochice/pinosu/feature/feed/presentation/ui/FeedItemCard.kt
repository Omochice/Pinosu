package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import io.github.omochice.pinosu.ui.component.ThumbnailRow
import java.time.Instant

private const val TITLE_MAX_LINES = 2

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
  Card(
      modifier = Modifier.fillMaxWidth().clickable { onOpen(item) },
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        ThumbnailRow(imageUrl = imageUrl, contentDescription = title) {
          FeedItemText(title = title, byline = feedItemByline(item), modifier = Modifier.weight(1f))
          onBookmark?.let { FeedItemBookmarkButton(onClick = it) }
        }
      }
}

@Composable
private fun FeedItemText(title: String, byline: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        maxLines = TITLE_MAX_LINES,
        overflow = TextOverflow.Ellipsis)
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
