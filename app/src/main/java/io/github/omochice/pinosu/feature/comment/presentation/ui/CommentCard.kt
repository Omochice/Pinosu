package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.comment.domain.model.Comment

/**
 * Card for displaying a NIP-22 kind 1111 comment or author comment
 *
 * Author comments use `primaryContainer` background with a label; regular comments use `surface`.
 *
 * @param comment The comment to display
 * @param profileImageUrl Profile image URL for the comment author, or null for fallback icon
 */
@Composable
internal fun CommentCard(comment: Comment, profileImageUrl: String? = null) {
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

          Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(
                imageUrl = profileImageUrl,
                contentDescription = stringResource(R.string.cd_commenter_avatar),
                size = 24.dp,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) { CommentBody(comment = comment) }
          }
        }
      }
}
