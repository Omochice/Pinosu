package io.github.omochice.pinosu.feature.bookmark.presentation.ui

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkItem
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkedEvent
import io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel.BookmarkFilterMode
import io.github.omochice.pinosu.feature.bookmark.presentation.viewmodel.BookmarkUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Composable function for bookmark list screen
 *
 * Displays a list of bookmarked items with pull-to-refresh support and tab-based filtering.
 * Supports both list and grid display modes based on user preference.
 *
 * @param uiState Bookmark screen UI state
 * @param onRefresh Callback when pull-to-refresh is triggered
 * @param onLoad Callback to load bookmarks on initial display
 * @param onOpenDrawer Callback when hamburger menu is clicked to open drawer
 * @param onTabSelected Callback when a filter tab is selected
 * @param onAddBookmark Callback when FAB is clicked to add a bookmark
 * @param onBookmarkDetailNavigate Callback when a bookmark card is tapped to navigate to detail
 * @param onLongPressBookmark Callback when a bookmark card is long-pressed with rawJson
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    uiState: BookmarkUiState,
    onRefresh: () -> Unit,
    onLoad: () -> Unit,
    onOpenDrawer: () -> Unit = {},
    onTabSelected: (BookmarkFilterMode) -> Unit = {},
    onAddBookmark: () -> Unit = {},
    onBookmarkDetailNavigate: (BookmarkItem) -> Unit = {},
    onLongPressBookmark: (String) -> Unit = {},
) {
  LaunchedEffect(Unit) { onLoad() }

  val clipboardManager = LocalClipboardManager.current
  val hapticFeedback = LocalHapticFeedback.current

  val onBookmarkClick: (BookmarkItem) -> Unit = { clickedBookmark ->
    onBookmarkDetailNavigate(clickedBookmark)
  }

  val onBookmarkLongPress: (BookmarkItem) -> Unit = { bookmark ->
    bookmark.rawJson?.let { rawJson ->
      clipboardManager.setClip(ClipEntry(ClipData.newPlainText("rawJson", rawJson)))
      hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
      onLongPressBookmark(rawJson)
    }
  }

  Scaffold(
      topBar = {
        BookmarkTopBar(
            selectedTab = uiState.selectedTab,
            onOpenDrawer = onOpenDrawer,
            onTabSelected = onTabSelected)
      },
      floatingActionButton = {
        FloatingActionButton(onClick = onAddBookmark) {
          Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = stringResource(R.string.cd_add_bookmark))
        }
      }) { paddingValues ->
        BookmarkContent(
            uiState = uiState,
            onRefresh = onRefresh,
            onBookmarkClick = onBookmarkClick,
            onBookmarkLongPress = onBookmarkLongPress,
            modifier = Modifier.padding(paddingValues).fillMaxSize())
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarkTopBar(
    selectedTab: BookmarkFilterMode,
    onOpenDrawer: () -> Unit,
    onTabSelected: (BookmarkFilterMode) -> Unit,
) {
  Column {
    TopAppBar(
        title = { Text(stringResource(R.string.title_bookmarks)) },
        navigationIcon = {
          IconButton(onClick = onOpenDrawer) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = stringResource(R.string.cd_open_menu))
          }
        })
    PrimaryTabRow(selectedTabIndex = if (selectedTab == BookmarkFilterMode.Local) 0 else 1) {
      Tab(
          selected = selectedTab == BookmarkFilterMode.Local,
          onClick = { onTabSelected(BookmarkFilterMode.Local) },
          text = { Text(stringResource(R.string.tab_local)) })
      Tab(
          selected = selectedTab == BookmarkFilterMode.Global,
          onClick = { onTabSelected(BookmarkFilterMode.Global) },
          text = { Text(stringResource(R.string.tab_global)) })
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarkContent(
    uiState: BookmarkUiState,
    onRefresh: () -> Unit,
    onBookmarkClick: (BookmarkItem) -> Unit,
    onBookmarkLongPress: (BookmarkItem) -> Unit,
    modifier: Modifier = Modifier,
) {
  PullToRefreshBox(isRefreshing = uiState.isLoading, onRefresh = onRefresh, modifier = modifier) {
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
        when (uiState.displayMode) {
          BookmarkDisplayMode.List ->
              BookmarkListView(
                  bookmarks = uiState.bookmarks,
                  onClick = onBookmarkClick,
                  onLongPress = onBookmarkLongPress)
          BookmarkDisplayMode.Grid ->
              BookmarkGridView(
                  bookmarks = uiState.bookmarks,
                  onClick = onBookmarkClick,
                  onLongPress = onBookmarkLongPress)
        }
      }
    }
  }
}

@Composable
private fun BookmarkListView(
    bookmarks: List<BookmarkItem>,
    onClick: (BookmarkItem) -> Unit,
    onLongPress: (BookmarkItem) -> Unit,
) {
  LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(
            bookmarks,
            key = { bookmark -> "${bookmark.type}:${bookmark.eventId ?: bookmark.hashCode()}" }) {
                bookmark ->
              BookmarkItemCard(bookmark = bookmark, onClick = onClick, onLongPress = onLongPress)
            }
      }
}

@Composable
private fun BookmarkGridView(
    bookmarks: List<BookmarkItem>,
    onClick: (BookmarkItem) -> Unit,
    onLongPress: (BookmarkItem) -> Unit,
) {
  LazyVerticalStaggeredGrid(
      columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalItemSpacing = 8.dp,
      horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(
            bookmarks,
            key = { bookmark -> "${bookmark.type}:${bookmark.eventId ?: bookmark.hashCode()}" }) {
                bookmark ->
              BookmarkItemCard(bookmark = bookmark, onClick = onClick, onLongPress = onLongPress)
            }
      }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookmarkItemCard(
    bookmark: BookmarkItem,
    onClick: (BookmarkItem) -> Unit,
    onLongPress: (BookmarkItem) -> Unit,
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
                  if (hasUrls) MaterialTheme.colorScheme.surface
                  else MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp)) {
          OgpThumbnail(imageUrl = bookmark.imageUrl)
          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
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
      }
}

private fun formatTimestamp(timestamp: Long): String {
  val instant = Instant.ofEpochSecond(timestamp)
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
  return formatter.format(instant)
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
              event =
                  BookmarkedEvent(
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
              event =
                  BookmarkedEvent(
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
