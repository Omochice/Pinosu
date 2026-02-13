package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.core.timestamp.formatTimestamp
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkItemCard(
    bookmark: BookmarkItem,
    onClick: (BookmarkItem) -> Unit,
    onLongPress: (BookmarkItem) -> Unit,
) {
  BookmarkCardShell(bookmark = bookmark, onClick = onClick, onLongPress = onLongPress) {
    Row(modifier = Modifier.padding(16.dp)) {
      OgpThumbnail(
          imageUrl = bookmark.imageUrl,
          contentDescription = bookmark.title,
          modifier = Modifier.size(80.dp))
      Spacer(modifier = Modifier.width(12.dp))
      BookmarkCardTextContent(bookmark = bookmark, modifier = Modifier.weight(1f))
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkGridItemCard(
    bookmark: BookmarkItem,
    onClick: (BookmarkItem) -> Unit,
    onLongPress: (BookmarkItem) -> Unit,
) {
  BookmarkCardShell(bookmark = bookmark, onClick = onClick, onLongPress = onLongPress) {
    Column {
      OgpThumbnail(
          imageUrl = bookmark.imageUrl,
          contentDescription = bookmark.title,
          modifier = Modifier.fillMaxWidth().height(100.dp))
      BookmarkCardTextContent(bookmark = bookmark, modifier = Modifier.padding(12.dp))
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookmarkCardShell(
    bookmark: BookmarkItem,
    onClick: (BookmarkItem) -> Unit,
    onLongPress: (BookmarkItem) -> Unit,
    content: @Composable () -> Unit,
) {
  val hasUrls = bookmark.urls.isNotEmpty()

  val clickModifier =
      if (hasUrls) {
        Modifier.combinedClickable(
            onClick = { onClick(bookmark) }, onLongClick = { onLongPress(bookmark) })
      } else {
        Modifier
      }

  Card(
      modifier = Modifier.fillMaxWidth().then(clickModifier),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (hasUrls) {
                    MaterialTheme.colorScheme.surface
                  } else {
                    MaterialTheme.colorScheme.surfaceVariant
                  })) {
        content()
      }
}

@Composable
private fun BookmarkCardTextContent(bookmark: BookmarkItem, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    bookmark.title?.let { title ->
      Text(
          text = title,
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis)
      Spacer(modifier = Modifier.height(8.dp))
    }

    if (bookmark.urls.isNotEmpty()) {
      bookmark.urls.forEach { url ->
        Text(
            text = url,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(vertical = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis)
      }

      Spacer(modifier = Modifier.height(8.dp))
    }

    bookmark.event?.let { event ->
      Text(
          text = formatTimestamp(event.createdAt),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}
