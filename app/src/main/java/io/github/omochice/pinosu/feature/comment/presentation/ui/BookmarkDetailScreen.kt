package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.comment.domain.model.Comment
import io.github.omochice.pinosu.feature.comment.presentation.viewmodel.BookmarkDetailUiState
import io.github.omochice.pinosu.ui.component.ErrorDialog
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Bookmark detail screen showing bookmark info and comments
 *
 * @param uiState Current UI state
 * @param title Bookmark title
 * @param urls List of bookmark URLs
 * @param createdAt Bookmark creation timestamp
 * @param onCommentInputChange Callback when comment input changes
 * @param onPostComment Callback when post button is clicked
 * @param onNavigateBack Callback to navigate back
 * @param onDismissError Callback to dismiss error dialog
 * @param onOpenUrlFailed Callback when opening a URL fails
 */
@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkDetailScreen(
    uiState: BookmarkDetailUiState,
    title: String?,
    urls: List<String>,
    createdAt: Long,
    onCommentInputChange: (String) -> Unit,
    onPostComment: () -> Unit,
    onNavigateBack: () -> Unit,
    onDismissError: () -> Unit,
    onOpenUrlFailed: () -> Unit = {},
) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = title ?: stringResource(R.string.title_bookmark_detail),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis)
            },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_navigate_up))
              }
            })
      },
      bottomBar = {
        CommentInputBar(
            value = uiState.commentInput,
            onValueChange = onCommentInputChange,
            onPost = onPostComment,
            isSubmitting = uiState.isSubmitting)
      }) { paddingValues ->
        BookmarkDetailContent(
            uiState = uiState,
            urls = urls,
            createdAt = createdAt,
            paddingValues = paddingValues,
            onOpenUrlFailed = onOpenUrlFailed)

        uiState.error?.let { error ->
          ErrorDialog(message = error.asString(), onDismiss = onDismissError)
        }
      }
}

@Suppress("LongParameterList")
@Composable
private fun BookmarkDetailContent(
    uiState: BookmarkDetailUiState,
    urls: List<String>,
    createdAt: Long,
    paddingValues: PaddingValues,
    onOpenUrlFailed: () -> Unit,
) {
  if (uiState.isLoading) {
    Box(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
  } else {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
          item {
            BookmarkInfoSection(
                urls = urls, createdAt = createdAt, onOpenUrlFailed = onOpenUrlFailed)
          }

          item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

          if (uiState.comments.isEmpty()) {
            item {
              Text(
                  text = stringResource(R.string.message_no_comments),
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(vertical = 16.dp))
            }
          } else {
            items(uiState.comments, key = { it.id }) { comment -> CommentCard(comment = comment) }
          }
        }
  }
}

@Composable
private fun BookmarkInfoSection(urls: List<String>, createdAt: Long, onOpenUrlFailed: () -> Unit) {
  val uriHandler = LocalUriHandler.current
  Column {
    urls.forEach { url ->
      Text(
          text = url,
          style =
              MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier =
              Modifier.padding(vertical = 2.dp).clickable {
                try {
                  uriHandler.openUri(url)
                } catch (_: Exception) {
                  onOpenUrlFailed()
                }
              })
    }

    if (createdAt > 0) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
          text = formatTimestamp(createdAt),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun CommentCard(comment: Comment) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (comment.isAuthorComment) {
                    MaterialTheme.colorScheme.primaryContainer
                  } else {
                    MaterialTheme.colorScheme.surface
                  })) {
        Column(modifier = Modifier.padding(12.dp)) {
          if (comment.isAuthorComment) {
            Text(
                text = stringResource(R.string.label_author_comment),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
          }

          Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)

          Spacer(modifier = Modifier.height(4.dp))

          Text(
              text = formatTimestamp(comment.createdAt),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
}

@Composable
private fun CommentInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onPost: () -> Unit,
    isSubmitting: Boolean,
) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .navigationBarsPadding()
              .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.hint_comment_input)) },
            maxLines = 3,
            enabled = !isSubmitting)

        Spacer(modifier = Modifier.width(8.dp))

        if (isSubmitting) {
          CircularProgressIndicator(
              modifier = Modifier.padding(8.dp),
          )
        } else {
          IconButton(onClick = onPost, enabled = value.isNotBlank()) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.cd_post_comment))
          }
        }
      }
}

private val timestampFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

private fun formatTimestamp(timestamp: Long): String =
    timestampFormatter.format(Instant.ofEpochSecond(timestamp))

@Preview(showBackground = true)
@Composable
private fun BookmarkDetailScreenPreview() {
  BookmarkDetailScreen(
      uiState =
          BookmarkDetailUiState(
              comments =
                  listOf(
                      Comment(
                          id = "author",
                          content = "This is my bookmark note",
                          authorPubkey = "pk1",
                          createdAt = 1_700_000_000L,
                          isAuthorComment = true),
                      Comment(
                          id = "c1",
                          content = "Nice bookmark!",
                          authorPubkey = "pk2",
                          createdAt = 1_700_000_100L,
                          isAuthorComment = false))),
      title = "Example Article",
      urls = listOf("https://example.com/article"),
      createdAt = 1_700_000_000L,
      onCommentInputChange = {},
      onPostComment = {},
      onNavigateBack = {},
      onDismissError = {})
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenEmptyPreview() {
  BookmarkDetailScreen(
      uiState = BookmarkDetailUiState(),
      title = "Empty Bookmark",
      urls = listOf("https://example.com"),
      createdAt = 1_700_000_000L,
      onCommentInputChange = {},
      onPostComment = {},
      onNavigateBack = {},
      onDismissError = {})
}
