package io.github.omochice.pinosu.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.domain.model.BookmarkItem
import io.github.omochice.pinosu.presentation.viewmodel.BookmarkUiState

/**
 * Bookmark list screen
 *
 * @param uiState Current UI state
 * @param onRefresh Callback when pull-to-refresh is triggered
 * @param onLoad Callback to load initial data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    uiState: BookmarkUiState,
    onRefresh: () -> Unit,
    onLoad: () -> Unit,
) {
  LaunchedEffect(Unit) { onLoad() }

  Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.title_bookmarks)) }) }) {
      paddingValues ->
    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.padding(paddingValues).fillMaxSize()) {
          when {
            uiState.isLoading && uiState.bookmarks.isEmpty() -> {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
              }
            }
            uiState.error != null && uiState.bookmarks.isEmpty() -> {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                      text = uiState.error,
                      style = MaterialTheme.typography.bodyLarge,
                      color = MaterialTheme.colorScheme.error)
                }
              }
            }
            uiState.bookmarks.isEmpty() -> {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.message_no_bookmarks),
                    style = MaterialTheme.typography.bodyLarge)
              }
            }
            else -> {
              LazyColumn(
                  modifier = Modifier.fillMaxSize(),
                  contentPadding = PaddingValues(16.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Show raw event JSON if available
                    uiState.rawEventJson?.let { json -> item { RawEventCard(json = json) } }
                    items(
                        uiState.bookmarks,
                        key = { bookmark ->
                          when (bookmark.type) {
                            "e" -> "e:${bookmark.eventId}"
                            "a" -> "a:${bookmark.articleCoordinate}"
                            "r" -> "r:${bookmark.url}"
                            "t" -> "t:${bookmark.hashtag}"
                            "q" -> "q:${bookmark.eventId}"
                            "p" -> "p:${bookmark.pubkey}"
                            "d" -> "d:${bookmark.identifier}"
                            else -> "${bookmark.type}:${bookmark.hashCode()}"
                          }
                        }) { bookmark ->
                          BookmarkItemCard(bookmark = bookmark)
                        }
                  }
            }
          }
        }
  }
}

/**
 * Card component for displaying raw event JSON
 *
 * @param json Raw event JSON string
 */
@Composable
private fun RawEventCard(json: String) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
              text = "Raw Event JSON",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
              text = json,
              style =
                  MaterialTheme.typography.bodySmall.copy(
                      fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
              modifier = Modifier.fillMaxWidth())
        }
      }
}

/**
 * Card component for a single bookmark item
 *
 * @param bookmark Bookmark item to display
 */
@Composable
private fun BookmarkItemCard(bookmark: BookmarkItem) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Type indicator
          Text(
              text = "Type: ${getBookmarkTypeDescription(bookmark.type)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.tertiary)
          Spacer(modifier = Modifier.height(8.dp))

          when (bookmark.type) {
            "e",
            "q" -> {
              // Event bookmark (e tag) or Quote (q tag)
              bookmark.event?.let { event ->
                // Event kind
                Text(
                    text = "Kind: ${event.kind} ${getKindDescription(event.kind)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                // Event content
                if (event.content.isNotEmpty()) {
                  Text(
                      text = event.content,
                      style = MaterialTheme.typography.bodyMedium,
                      maxLines = 3,
                      overflow = TextOverflow.Ellipsis)
                  Spacer(modifier = Modifier.height(8.dp))
                }

                // Author
                Text(
                    text = "Author: ${formatEventId(event.author)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp
                Text(
                    text = formatTimestamp(event.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
                  ?: run {
                    // Fallback if event not fetched
                    bookmark.eventId?.let { eventId ->
                      Text(
                          text = "Event ID: ${formatEventId(eventId)}",
                          style = MaterialTheme.typography.bodyMedium)
                    }
                  }
            }
            "a" -> {
              // Article/parameterized replaceable event bookmark
              bookmark.articleCoordinate?.let { coordinate ->
                Text(
                    text = "Article",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = coordinate,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
              }
            }
            "r" -> {
              // URL bookmark
              bookmark.url?.let { url ->
                Text(
                    text = "URL",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis)
              }
            }
            "t" -> {
              // Hashtag bookmark
              bookmark.hashtag?.let { hashtag ->
                Text(
                    text = "Hashtag",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "#$hashtag",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary)
              }
            }
            "p" -> {
              // Pubkey reference
              bookmark.pubkey?.let { pubkey ->
                Text(
                    text = "User",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatEventId(pubkey),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
              }
            }
            "d" -> {
              // Identifier
              bookmark.identifier?.let { identifier ->
                Text(
                    text = "Identifier",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = identifier,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis)
              }
            }
          }

          // Relay URL
          bookmark.relayUrl?.let { relay ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.label_relay),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = relay,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary)
          }
        }
      }
}

/**
 * Get bookmark type description
 *
 * @param type Bookmark type (e, a, r, t, q, p, d)
 * @return Human-readable description
 */
private fun getBookmarkTypeDescription(type: String): String {
  return when (type) {
    "e" -> "Event"
    "a" -> "Article"
    "r" -> "URL"
    "t" -> "Hashtag"
    "q" -> "Quote"
    "p" -> "User"
    "d" -> "Identifier"
    else -> "Unknown"
  }
}

/**
 * Format event ID for display
 *
 * @param eventId Full event ID
 * @return Formatted event ID (truncated)
 */
private fun formatEventId(eventId: String): String {
  return if (eventId.length > 16) {
    "${eventId.take(8)}...${eventId.takeLast(8)}"
  } else {
    eventId
  }
}

/**
 * Get human-readable description for event kind
 *
 * @param kind Event kind number
 * @return Description string
 */
private fun getKindDescription(kind: Int): String {
  return when (kind) {
    0 -> "(Metadata)"
    1 -> "(Text Note)"
    2 -> "(Recommend Relay)"
    3 -> "(Contacts)"
    4 -> "(Encrypted DM)"
    5 -> "(Event Deletion)"
    6 -> "(Repost)"
    7 -> "(Reaction)"
    40 -> "(Channel Creation)"
    41 -> "(Channel Metadata)"
    42 -> "(Channel Message)"
    43 -> "(Channel Hide Message)"
    44 -> "(Channel Mute User)"
    in 10000..19999 -> "(Replaceable)"
    in 20000..29999 -> "(Ephemeral)"
    in 30000..39999 -> "(Parameterized Replaceable)"
    else -> ""
  }
}

/**
 * Format Unix timestamp to readable date/time
 *
 * @param timestamp Unix timestamp in seconds
 * @return Formatted date/time string
 */
private fun formatTimestamp(timestamp: Long): String {
  val date = java.util.Date(timestamp * 1000)
  val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
  return formatter.format(date)
}

@Preview(showBackground = true)
@Composable
private fun BookmarkScreenLoadingPreview() {
  BookmarkScreen(uiState = BookmarkUiState(isLoading = true), onRefresh = {}, onLoad = {})
}

@Preview(showBackground = true)
@Composable
private fun BookmarkScreenEmptyPreview() {
  BookmarkScreen(
      uiState = BookmarkUiState(isLoading = false, bookmarks = emptyList()),
      onRefresh = {},
      onLoad = {})
}

@Preview(showBackground = true)
@Composable
private fun BookmarkScreenWithDataPreview() {
  val sampleBookmarks =
      listOf(
          BookmarkItem(
              type = "e",
              eventId = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
              relayUrl = "wss://relay.example.com"),
          BookmarkItem(type = "r", url = "https://example.com/article"),
          BookmarkItem(type = "t", hashtag = "nostr"),
          BookmarkItem(type = "a", articleCoordinate = "30023:pubkey:d-identifier"),
      )
  BookmarkScreen(
      uiState = BookmarkUiState(isLoading = false, bookmarks = sampleBookmarks),
      onRefresh = {},
      onLoad = {})
}
