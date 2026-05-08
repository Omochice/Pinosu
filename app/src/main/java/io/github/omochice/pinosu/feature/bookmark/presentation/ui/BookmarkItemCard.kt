package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.core.timestamp.formatTimestamp
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkItemCard(
    bookmark: BookmarkItem,
    onClick: (BookmarkItem) -> Unit,
    onLongPress: (BookmarkItem) -> Unit,
    onCopyNostrLink: ((BookmarkItem) -> Unit)? = null,
) {
  BookmarkCardShell(
      bookmark = bookmark,
      onClick = onClick,
      onLongPress = onLongPress,
      onCopyNostrLink = onCopyNostrLink) {
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
    onCopyNostrLink: ((BookmarkItem) -> Unit)? = null,
) {
  BookmarkCardShell(
      bookmark = bookmark,
      onClick = onClick,
      onLongPress = onLongPress,
      onCopyNostrLink = onCopyNostrLink) {
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
    onCopyNostrLink: ((BookmarkItem) -> Unit)?,
    content: @Composable () -> Unit,
) {
  val hasUrls = bookmark.urls.isNotEmpty()
  var showMenu by remember { mutableStateOf(false) }

  val clickModifier =
      if (hasUrls) {
        Modifier.combinedClickable(
            onClick = { onClick(bookmark) }, onLongClick = { showMenu = true })
      } else {
        Modifier
      }

  Box {
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

    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
      DropdownMenuItem(
          text = { Text(stringResource(R.string.menu_copy_raw_json)) },
          onClick = {
            onLongPress(bookmark)
            showMenu = false
          })
      onCopyNostrLink?.let { handler ->
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_copy_nostr_link)) },
            onClick = {
              handler(bookmark)
              showMenu = false
            })
      }
    }
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
