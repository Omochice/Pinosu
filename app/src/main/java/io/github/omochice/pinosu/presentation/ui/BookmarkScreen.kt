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

/** Bookmark list screen */
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
                    items(
                        uiState.bookmarks,
                        key = { bookmark ->
                          "${bookmark.type}:${bookmark.eventId ?: bookmark.hashCode()}"
                        }) { bookmark ->
                          BookmarkItemCard(bookmark = bookmark)
                        }
                  }
            }
          }
        }
  }
}

@Composable
private fun BookmarkItemCard(bookmark: BookmarkItem) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          bookmark.title?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
          }

          bookmark.event?.content?.let { description ->
            if (description.isNotEmpty()) {
              Text(
                  text = description,
                  style = MaterialTheme.typography.bodyMedium,
                  maxLines = 3,
                  overflow = TextOverflow.Ellipsis)
              Spacer(modifier = Modifier.height(12.dp))
            }
          }

          if (bookmark.urls.isNotEmpty()) {
            Text(
                text = "URLs (${bookmark.urls.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary)
            Spacer(modifier = Modifier.height(4.dp))

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

          if (bookmark.titleSource == "metadata") {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "(Title from OG metadata)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary)
          }
        }
      }
}

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
              type = "event",
              eventId = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
              title = "Example Article Title",
              url = "https://example.com/article",
              urls = listOf("https://example.com/article", "https://example.com/related"),
              titleSource = "tag",
              event =
                  io.github.omochice.pinosu.domain.model.BookmarkedEvent(
                      kind = 39701,
                      content = "This is a sample bookmark description from the event content.",
                      author = "abcd1234abcd1234",
                      createdAt = System.currentTimeMillis() / 1000,
                      tags = emptyList())),
          BookmarkItem(
              type = "event",
              eventId = "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890",
              title = "Another Bookmark",
              url = "https://example.com/another",
              urls = listOf("https://example.com/another"),
              titleSource = "metadata",
              event =
                  io.github.omochice.pinosu.domain.model.BookmarkedEvent(
                      kind = 39701,
                      content = "Fetched title from OG metadata.",
                      author = "efgh5678efgh5678",
                      createdAt = System.currentTimeMillis() / 1000 - 3600,
                      tags = emptyList())),
      )
  BookmarkScreen(
      uiState = BookmarkUiState(isLoading = false, bookmarks = sampleBookmarks),
      onRefresh = {},
      onLoad = {})
}
