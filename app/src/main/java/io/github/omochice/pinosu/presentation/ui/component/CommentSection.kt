package io.github.omochice.pinosu.presentation.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.comments.model.Comment
import io.github.omochice.pinosu.feature.comments.model.CommentLoadState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Expandable comment section component
 *
 * Displays a toggle button that expands to show comments when clicked. Shows loading indicator
 * while fetching, comment list on success, or error message on failure.
 *
 * @param isExpanded Whether the comment section is currently expanded
 * @param loadState Current loading state of comments (null if not yet fetched)
 * @param onToggle Callback when toggle button is clicked
 */
@Composable
internal fun CommentSection(
    isExpanded: Boolean,
    loadState: CommentLoadState?,
    onToggle: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

    TextButton(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector =
                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text =
                stringResource(if (isExpanded) R.string.hide_comments else R.string.show_comments))
      }
    }

    AnimatedVisibility(
        visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
          Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            when (loadState) {
              is CommentLoadState.Loading -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                      CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                          text = stringResource(R.string.loading_comments),
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
              }
              is CommentLoadState.Success -> {
                if (loadState.comments.isEmpty()) {
                  Text(
                      text = stringResource(R.string.no_comments),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(vertical = 8.dp))
                } else {
                  loadState.comments.forEach { comment -> CommentItem(comment = comment) }
                }
              }
              is CommentLoadState.Error -> {
                Text(
                    text = loadState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp))
              }
              null -> {
                Text(
                    text = stringResource(R.string.loading_comments),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp))
              }
            }
          }
        }
  }
}

@Composable
private fun CommentItem(comment: Comment) {
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
    Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)
    Spacer(modifier = Modifier.height(4.dp))
    Row {
      Text(
          text = comment.author.take(8) + "...",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
      Spacer(modifier = Modifier.width(8.dp))
      Text(
          text = formatCommentTimestamp(comment.createdAt),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
  }
}

private fun formatCommentTimestamp(timestamp: Long): String {
  val instant = Instant.ofEpochSecond(timestamp)
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
  return formatter.format(instant)
}

@Preview(showBackground = true)
@Composable
private fun CommentSectionCollapsedPreview() {
  CommentSection(isExpanded = false, loadState = null, onToggle = {})
}

@Preview(showBackground = true)
@Composable
private fun CommentSectionLoadingPreview() {
  CommentSection(isExpanded = true, loadState = CommentLoadState.Loading, onToggle = {})
}

@Preview(showBackground = true)
@Composable
private fun CommentSectionEmptyPreview() {
  CommentSection(
      isExpanded = true, loadState = CommentLoadState.Success(emptyList()), onToggle = {})
}

@Preview(showBackground = true)
@Composable
private fun CommentSectionWithCommentsPreview() {
  val comments =
      listOf(
          Comment(
              id = "1",
              content = "This is a great bookmark!",
              author = "abcd1234abcd1234abcd1234abcd1234",
              createdAt = System.currentTimeMillis() / 1000,
              referencedEventId = "event1"),
          Comment(
              id = "2",
              content = "Very useful resource, thanks for sharing.",
              author = "efgh5678efgh5678efgh5678efgh5678",
              createdAt = System.currentTimeMillis() / 1000 - 3600,
              referencedEventId = "event1"))
  CommentSection(isExpanded = true, loadState = CommentLoadState.Success(comments), onToggle = {})
}

@Preview(showBackground = true)
@Composable
private fun CommentSectionErrorPreview() {
  CommentSection(
      isExpanded = true, loadState = CommentLoadState.Error("Network error"), onToggle = {})
}
