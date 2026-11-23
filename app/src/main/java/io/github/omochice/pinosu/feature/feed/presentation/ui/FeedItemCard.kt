package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import java.time.Instant

private const val TITLE_MAX_LINES = 2
private const val SUMMARY_MAX_LINES = 3

/**
 * Card showing one feed item
 *
 * @param item Item to show
 * @param onOpen Callback when the card is tapped
 * @param onBookmark Callback when the bookmark action is tapped, or null to hide the action
 */
@Composable
fun FeedItemCard(
    item: FeedItem,
    onOpen: (FeedItem) -> Unit,
    onBookmark: ((FeedItem) -> Unit)?,
) {
  Card(
      modifier = Modifier.fillMaxWidth().clickable { onOpen(item) },
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
          FeedItemText(item = item, modifier = Modifier.weight(1f))
          onBookmark?.let { bookmark ->
            IconButton(onClick = { bookmark(item) }) {
              Icon(
                  imageVector = Icons.Outlined.BookmarkAdd,
                  contentDescription = stringResource(R.string.cd_bookmark_feed_item))
            }
          }
        }
      }
}

@Composable
private fun FeedItemText(item: FeedItem, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text(
        text = item.title.ifEmpty { item.link.orEmpty() },
        style = MaterialTheme.typography.titleMedium,
        maxLines = TITLE_MAX_LINES,
        overflow = TextOverflow.Ellipsis)
    Text(
        text = feedItemByline(item),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis)
    if (item.summary.isNotBlank()) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
          text = item.summary,
          style = MaterialTheme.typography.bodyMedium,
          maxLines = SUMMARY_MAX_LINES,
          overflow = TextOverflow.Ellipsis)
    }
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
      onOpen = {},
      onBookmark = {})
}
