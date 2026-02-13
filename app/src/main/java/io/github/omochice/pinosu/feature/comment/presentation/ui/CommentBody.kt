package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.core.timestamp.formatTimestamp
import io.github.omochice.pinosu.feature.comment.domain.model.Comment

/**
 * Shared content body for comment cards, rendering content text and formatted timestamp
 *
 * @param comment The comment whose content and timestamp to display
 */
@Composable
internal fun CommentBody(comment: Comment) {
  Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)

  Spacer(modifier = Modifier.height(4.dp))

  Text(
      text = formatTimestamp(comment.createdAt),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant)
}
