package io.github.omochice.pinosu.presentation.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.domain.model.BookmarkItem
import io.github.omochice.pinosu.domain.model.BookmarkedEvent
import io.github.omochice.pinosu.presentation.ui.component.ErrorDialog
import io.github.omochice.pinosu.presentation.ui.component.UrlSelectionDialog
import io.github.omochice.pinosu.presentation.viewmodel.BookmarkFilterMode
import io.github.omochice.pinosu.presentation.viewmodel.BookmarkUiState
import io.github.omochice.pinosu.presentation.viewmodel.BookmarkViewModel
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
 * @param viewModel ViewModel for bookmark screen (null for previews)
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
    viewModel: BookmarkViewModel? = null,
) {
  LaunchedEffect(Unit) { onLoad() }

  val uriHandler = LocalUriHandler.current
  val urlOpenErrorText = stringResource(R.string.error_url_open_failed)

  val onBookmarkClick: (BookmarkItem) -> Unit = { clickedBookmark ->
    viewModel?.let { vm ->
      if (clickedBookmark.urls.size == 1) {
        try {
          uriHandler.openUri(clickedBookmark.urls.first())
        } catch (e: Exception) {
          vm.setUrlOpenError(e.message ?: urlOpenErrorText)
        }
      } else {
        vm.onBookmarkCardClicked(clickedBookmark)
      }
    }
  }

  Scaffold(
      topBar = {
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
          PrimaryTabRow(
              selectedTabIndex = if (uiState.selectedTab == BookmarkFilterMode.Local) 0 else 1) {
                Tab(
                    selected = uiState.selectedTab == BookmarkFilterMode.Local,
                    onClick = { onTabSelected(BookmarkFilterMode.Local) },
                    text = { Text(stringResource(R.string.tab_local)) })
                Tab(
                    selected = uiState.selectedTab == BookmarkFilterMode.Global,
                    onClick = { onTabSelected(BookmarkFilterMode.Global) },
                    text = { Text(stringResource(R.string.tab_global)) })
              }
        }
      },
      floatingActionButton = {
        FloatingActionButton(onClick = onAddBookmark) {
          Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = stringResource(R.string.cd_add_bookmark))
        }
      }) { paddingValues ->
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
                  when (uiState.displayMode) {
                    BookmarkDisplayMode.List ->
                        BookmarkListView(bookmarks = uiState.bookmarks, onClick = onBookmarkClick)
                    BookmarkDisplayMode.Grid ->
                        BookmarkGridView(bookmarks = uiState.bookmarks, onClick = onBookmarkClick)
                  }
                }
              }
            }

        viewModel?.let { vm ->
          uiState.selectedBookmarkForUrlDialog?.let { bookmark ->
            UrlSelectionDialog(
                urls = bookmark.urls,
                onUrlSelected = { url ->
                  vm.dismissUrlDialog()
                  try {
                    uriHandler.openUri(url)
                  } catch (e: Exception) {
                    vm.setUrlOpenError(e.message ?: urlOpenErrorText)
                  }
                },
                onDismiss = { vm.dismissUrlDialog() })
          }

          uiState.urlOpenError?.let { error ->
            ErrorDialog(message = error, onDismiss = { vm.dismissErrorDialog() })
          }
        }
      }
}

@Composable
private fun BookmarkListView(bookmarks: List<BookmarkItem>, onClick: (BookmarkItem) -> Unit) {
  LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(
            bookmarks,
            key = { bookmark -> "${bookmark.type}:${bookmark.eventId ?: bookmark.hashCode()}" }) {
                bookmark ->
              BookmarkItemCard(bookmark = bookmark, onClick = onClick)
            }
      }
}

@Composable
private fun BookmarkGridView(bookmarks: List<BookmarkItem>, onClick: (BookmarkItem) -> Unit) {
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
              BookmarkItemCard(bookmark = bookmark, onClick = onClick)
            }
      }
}

@Composable
private fun BookmarkItemCard(bookmark: BookmarkItem, onClick: (BookmarkItem) -> Unit) {
  val hasUrls = bookmark.urls.isNotEmpty()

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .then(if (hasUrls) Modifier.clickable { onClick(bookmark) } else Modifier),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (hasUrls) MaterialTheme.colorScheme.surface
                  else MaterialTheme.colorScheme.surfaceVariant)) {
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
                text = stringResource(R.string.url_count, bookmark.urls.size),
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
