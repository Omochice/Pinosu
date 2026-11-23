package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.feed.domain.model.FeedFetchFailure
import io.github.omochice.pinosu.feature.feed.domain.model.FeedItem
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedLoadError
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedUiState
import kotlinx.coroutines.launch

/**
 * Composable function for the feed timeline screen
 *
 * @param uiState Feed screen UI state
 * @param onRefresh Callback when pull-to-refresh or retry is triggered
 * @param onOpenDrawer Callback when hamburger menu is clicked to open drawer
 * @param onBookmarkItem Callback with the link and title of the item whose bookmark action is
 *   tapped
 * @param onManageSubscriptions Callback when the user asks to change the subscribed feeds
 * @param isReadOnly Whether to hide the bookmark action and the subscription entry points for
 *   read-only login
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    uiState: FeedUiState,
    onRefresh: () -> Unit,
    onOpenDrawer: () -> Unit = {},
    onBookmarkItem: (link: String, title: String) -> Unit = { _, _ -> },
    onManageSubscriptions: () -> Unit = {},
    isReadOnly: Boolean = false,
) {
  val manageSubscriptions = if (isReadOnly) null else onManageSubscriptions
  val uriHandler = LocalUriHandler.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val openFailedMessage = stringResource(R.string.error_url_open_failed)

  val callbacks =
      FeedItemCallbacks(
          onOpen = { item ->
            item.link?.let { link ->
              try {
                uriHandler.openUri(link)
              } catch (_: IllegalArgumentException) {
                scope.launch { snackbarHostState.showSnackbar(openFailedMessage) }
              }
            }
          },
          onBookmark =
              if (isReadOnly) {
                null
              } else {
                { item -> item.link?.let { link -> onBookmarkItem(link, item.title) } }
              })

  Scaffold(
      topBar = {
        FeedTopBar(onOpenDrawer = onOpenDrawer, onManageSubscriptions = manageSubscriptions)
      },
      snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.padding(paddingValues).fillMaxSize()) {
              FeedContent(
                  content = uiState.content,
                  callbacks = callbacks,
                  onRetry = onRefresh,
                  onAddFeed = manageSubscriptions)
            }
      }
}

@Composable
private fun FeedContent(
    content: FeedUiState.Content,
    callbacks: FeedItemCallbacks,
    onRetry: () -> Unit,
    onAddFeed: (() -> Unit)?,
) {
  when (content) {
    FeedUiState.Content.Loading ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
    FeedUiState.Content.Empty -> ScrollableCenteredBox { FeedEmpty(onAddFeed = onAddFeed) }
    is FeedUiState.Content.Error ->
        ScrollableCenteredBox { FeedError(reason = content.reason, onRetry = onRetry) }
    is FeedUiState.Content.Loaded ->
        FeedList(items = content.items, failedFeeds = content.failedFeeds, callbacks = callbacks)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedTopBar(onOpenDrawer: () -> Unit, onManageSubscriptions: (() -> Unit)?) {
  TopAppBar(
      title = { Text(stringResource(R.string.title_feeds)) },
      navigationIcon = {
        IconButton(onClick = onOpenDrawer) {
          Icon(
              imageVector = Icons.Default.Menu,
              contentDescription = stringResource(R.string.cd_open_menu))
        }
      },
      actions = {
        onManageSubscriptions?.let { manage ->
          IconButton(onClick = manage) {
            Icon(
                imageVector = Icons.Outlined.RssFeed,
                contentDescription = stringResource(R.string.cd_manage_feed_subscriptions))
          }
        }
      })
}

@Composable
private fun FeedEmpty(onAddFeed: (() -> Unit)?) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
        text = stringResource(R.string.message_no_feeds),
        style = MaterialTheme.typography.bodyLarge)
    onAddFeed?.let { add ->
      Spacer(modifier = Modifier.height(16.dp))
      Button(onClick = add) { Text(stringResource(R.string.button_add_first_feed)) }
    }
  }
}

/** Centers [content] inside a scrollable list so that PullToRefreshBox still receives the pull */
@Composable
private fun ScrollableCenteredBox(content: @Composable () -> Unit) {
  LazyColumn(modifier = Modifier.fillMaxSize()) {
    item {
      Box(
          modifier = Modifier.fillParentMaxSize().padding(16.dp),
          contentAlignment = Alignment.Center) {
            content()
          }
    }
  }
}

@Composable
private fun FeedError(reason: FeedLoadError, onRetry: () -> Unit) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
        text = stringResource(reason.messageRes()),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onRetry) { Text(stringResource(R.string.button_retry)) }
  }
}

@Composable
private fun FeedList(
    items: List<FeedItem>,
    failedFeeds: List<FeedFetchFailure>,
    callbacks: FeedItemCallbacks,
) {
  LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (failedFeeds.isNotEmpty()) {
          item(key = "failedFeeds") { FailedFeedsNotice(failedFeeds = failedFeeds) }
        }
        items(items, key = ::feedItemKey) { item ->
          FeedItemCard(
              item = item,
              onOpen = callbacks.onOpen,
              onBookmark = callbacks.onBookmark?.takeIf { item.link != null })
        }
      }
}

/** Lazy list key of [item]; its id is unique only within its own feed */
private fun feedItemKey(item: FeedItem): String = "${item.feedUrl}\n${item.id}"

@Preview(showBackground = true)
@Composable
private fun FeedScreenEmptyPreview() {
  FeedScreen(uiState = FeedUiState(content = FeedUiState.Content.Empty), onRefresh = {})
}

@Preview(showBackground = true)
@Composable
private fun FeedScreenErrorPreview() {
  FeedScreen(
      uiState = FeedUiState(content = FeedUiState.Content.Error(FeedLoadError.Unreachable)),
      onRefresh = {})
}
