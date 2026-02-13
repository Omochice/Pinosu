package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.core.timestamp.formatTimestamp
import io.github.omochice.pinosu.feature.comment.domain.model.Comment

/**
 * Card for displaying a NIP-22 kind 1111 comment or author comment
 *
 * Author comments use `primaryContainer` background with a label; regular comments use `surface`.
 *
 * @param comment The comment to display
 */
@Composable
internal fun CommentCard(comment: Comment) {
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
