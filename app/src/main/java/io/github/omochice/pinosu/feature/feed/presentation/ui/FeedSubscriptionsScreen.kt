package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscription
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedLoadError
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedSubscriptionsUiState
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.orderedCategories

private const val FEED_URL_MAX_LINES = 2

/**
 * Composable function for the screen listing the feeds the user subscribes to
 *
 * @param uiState Subscription screen UI state
 * @param callbacks Callbacks for the user's actions
 * @param isReadOnly Whether to hide adding, removing, and filing feeds for read-only login
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedSubscriptionsScreen(
    uiState: FeedSubscriptionsUiState,
    callbacks: FeedSubscriptionsCallbacks,
    isReadOnly: Boolean = false,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  var isAddDialogOpen by rememberSaveable { mutableStateOf(false) }
  var categoryDialogFeedUrl by rememberSaveable { mutableStateOf<String?>(null) }
  val messageText = uiState.message?.let { stringResource(it.messageRes()) }

  LaunchedEffect(messageText) {
    if (messageText != null) {
      snackbarHostState.showSnackbar(messageText)
      callbacks.onMessageShown()
    }
  }

  Scaffold(
      topBar = {
        FeedSubscriptionsTopBar(
            onNavigateUp = callbacks.onNavigateUp,
            onAdd = { isAddDialogOpen = true }.takeIf { !isReadOnly },
            isAddEnabled =
                uiState.content is FeedSubscriptionsUiState.Content.Loaded && !uiState.isWorking)
      },
      snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
          if (uiState.isWorking) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
          }
          FeedSubscriptionsContent(
              content = uiState.content,
              onRetry = callbacks.onRetry,
              rowActions =
                  if (isReadOnly) {
                    null
                  } else {
                    FeedRowActions(
                        onEditCategory = { categoryDialogFeedUrl = it },
                        onRemove = callbacks.onRemove)
                  },
              isEnabled = !uiState.isWorking)
        }
      }

  if (isAddDialogOpen) {
    AddFeedDialog(
        onSubmit = { input ->
          isAddDialogOpen = false
          callbacks.onAdd(input)
        },
        onDismiss = { isAddDialogOpen = false })
  }
  categoryDialogFeedUrl?.let { feedUrl ->
    FeedCategoryDialogFor(
        feedUrl = feedUrl,
        content = uiState.content,
        onSetCategory = callbacks.onSetCategory,
        onClose = { categoryDialogFeedUrl = null })
  }
  FeedSubscriptionsDialogHost(dialog = uiState.dialog, callbacks = callbacks)
}

/** Actions offered on each subscribed feed when the user may change the list */
private data class FeedRowActions(
    val onEditCategory: (feedUrl: String) -> Unit,
    val onRemove: (feedUrl: String) -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedSubscriptionsTopBar(
    onNavigateUp: () -> Unit,
    onAdd: (() -> Unit)?,
    isAddEnabled: Boolean,
) {
  TopAppBar(
      title = { Text(stringResource(R.string.title_feed_subscriptions)) },
      navigationIcon = {
        IconButton(onClick = onNavigateUp) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.cd_navigate_up))
        }
      },
      actions = {
        onAdd?.let { add ->
          IconButton(onClick = add, enabled = isAddEnabled) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.cd_add_feed))
          }
        }
      })
}

@Composable
private fun FeedCategoryDialogFor(
    feedUrl: String,
    content: FeedSubscriptionsUiState.Content,
    onSetCategory: (feedUrl: String, category: String?) -> Unit,
    onClose: () -> Unit,
) {
  val feeds = (content as? FeedSubscriptionsUiState.Content.Loaded)?.feeds.orEmpty()
  val currentCategory = feeds.firstOrNull { it.url == feedUrl }?.category
  FeedCategoryDialog(
      currentCategory = currentCategory,
      suggestions =
          orderedCategories(feeds.mapNotNull { it.category }) - setOfNotNull(currentCategory),
      onSave = { category ->
        onClose()
        onSetCategory(feedUrl, category)
      },
      onDismiss = onClose)
}

@Composable
private fun FeedSubscriptionsContent(
    content: FeedSubscriptionsUiState.Content,
    onRetry: () -> Unit,
    rowActions: FeedRowActions?,
    isEnabled: Boolean,
) {
  when (content) {
    FeedSubscriptionsUiState.Content.Loading ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
    is FeedSubscriptionsUiState.Content.Error ->
        CenteredMessage(text = stringResource(content.reason.messageRes())) {
          Button(onClick = onRetry) { Text(stringResource(R.string.button_retry)) }
        }
    is FeedSubscriptionsUiState.Content.Loaded ->
        if (content.feeds.isEmpty()) {
          CenteredMessage(text = stringResource(R.string.message_no_feeds))
        } else {
          LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(content.feeds, key = { it.url }) { feed ->
              FeedSubscriptionRow(feed = feed, actions = rowActions, isEnabled = isEnabled)
            }
          }
        }
  }
}

@Composable
private fun CenteredMessage(text: String, action: (@Composable () -> Unit)? = null) {
  Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(text = text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
      action?.let {
        Spacer(modifier = Modifier.height(16.dp))
        it()
      }
    }
  }
}

@Composable
private fun FeedSubscriptionRow(
    feed: FeedSubscription,
    actions: FeedRowActions?,
    isEnabled: Boolean,
) {
  ListItem(
      headlineContent = {
        Text(text = feed.url, maxLines = FEED_URL_MAX_LINES, overflow = TextOverflow.Ellipsis)
      },
      supportingContent = feed.category?.let { category -> { Text(text = category) } },
      trailingContent =
          actions?.let { rowActions ->
            {
              Row {
                IconButton(onClick = { rowActions.onEditCategory(feed.url) }, enabled = isEnabled) {
                  Icon(
                      imageVector = Icons.AutoMirrored.Outlined.Label,
                      contentDescription = stringResource(R.string.cd_set_feed_category))
                }
                IconButton(onClick = { rowActions.onRemove(feed.url) }, enabled = isEnabled) {
                  Icon(
                      imageVector = Icons.Outlined.Delete,
                      contentDescription = stringResource(R.string.cd_remove_feed))
                }
              }
            }
          })
}

@Preview(showBackground = true)
@Composable
private fun SubscriptionsPreview() {
  FeedSubscriptionsScreen(
      uiState =
          FeedSubscriptionsUiState(
              content =
                  FeedSubscriptionsUiState.Content.Loaded(
                      listOf(
                          FeedSubscription("https://blog.example/feed", "Tech"),
                          FeedSubscription("https://news.example/atom.xml")))),
      callbacks = previewCallbacks)
}

@Preview(showBackground = true)
@Composable
private fun SubscriptionsErrorPreview() {
  FeedSubscriptionsScreen(
      uiState =
          FeedSubscriptionsUiState(
              content = FeedSubscriptionsUiState.Content.Error(FeedLoadError.Unreachable)),
      callbacks = previewCallbacks)
}

private val previewCallbacks =
    FeedSubscriptionsCallbacks(
        onNavigateUp = {},
        onRetry = {},
        onAdd = {},
        onRemove = {},
        onSetCategory = { _, _ -> },
        onConfirmPublicNotice = {},
        onDismissDialog = {},
        onMessageShown = {})
