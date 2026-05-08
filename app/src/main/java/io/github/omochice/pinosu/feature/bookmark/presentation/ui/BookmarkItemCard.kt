package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.core.timestamp.formatTimestamp
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkItemCard(
    bookmark: BookmarkItem,
    onClick: (BookmarkItem) -> Unit,
    onLongPress: ((BookmarkItem) -> Unit)?,
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
    onLongPress: ((BookmarkItem) -> Unit)?,
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
    onLongPress: ((BookmarkItem) -> Unit)?,
    onCopyNostrLink: ((BookmarkItem) -> Unit)?,
    content: @Composable () -> Unit,
) {
  val hasUrls = bookmark.urls.isNotEmpty()
  val hasMenuItems = onLongPress != null || onCopyNostrLink != null
  var showMenu by remember { mutableStateOf(false) }
  var pressOffset by remember { mutableStateOf(Offset.Zero) }
  val interactionSource = remember { MutableInteractionSource() }
  val density = LocalDensity.current

  LaunchedEffect(interactionSource) {
    interactionSource.interactions.collect { interaction ->
      if (interaction is PressInteraction.Press) {
        pressOffset = interaction.pressPosition
      }
    }
  }

  val onLongClickHandler: (() -> Unit)? =
      if (hasMenuItems) {
        { showMenu = true }
      } else {
        null
      }
  val clickModifier =
      if (hasUrls) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = { onClick(bookmark) },
            onLongClick = onLongClickHandler)
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

    BookmarkLongPressMenu(
        expanded = showMenu,
        offset = with(density) { DpOffset(pressOffset.x.toDp(), pressOffset.y.toDp()) },
        onDismiss = { showMenu = false },
        onCopyRawJson = onLongPress?.let { handler -> { handler(bookmark) } },
        onCopyNostrLink = onCopyNostrLink?.let { handler -> { handler(bookmark) } })
  }
}

@Composable
private fun BookmarkLongPressMenu(
    expanded: Boolean,
    offset: DpOffset,
    onDismiss: () -> Unit,
    onCopyRawJson: (() -> Unit)?,
    onCopyNostrLink: (() -> Unit)?,
) {
  DropdownMenu(expanded = expanded, onDismissRequest = onDismiss, offset = offset) {
    onCopyRawJson?.let { handler ->
      DropdownMenuItem(
          text = { Text(stringResource(R.string.menu_copy_raw_json)) },
          onClick = {
            handler()
            onDismiss()
          })
    }
    onCopyNostrLink?.let { handler ->
      DropdownMenuItem(
          text = { Text(stringResource(R.string.menu_copy_nostr_link)) },
          onClick = {
            handler()
            onDismiss()
          })
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
