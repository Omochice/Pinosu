package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.comment.domain.model.Comment

/**
 * Card with blockquote-style decoration for kind 1 (text note) events
 *
 * Uses a left vertical bar as the sole visual indicator to distinguish quotes from regular
 * comments.
 *
 * @param comment The kind 1 comment to display
 * @param profileImageUrl Profile image URL for the text note author, or null for fallback icon
 */
@Composable
internal fun QuoteCard(comment: Comment, profileImageUrl: String? = null) {
  val primaryColor = MaterialTheme.colorScheme.primary
  Surface(
      modifier =
          Modifier.fillMaxWidth().drawWithContent {
            drawContent()
            val barWidth = 4.dp.toPx()
            drawRect(
                color = primaryColor, topLeft = Offset.Zero, size = size.copy(width = barWidth))
          },
      color = MaterialTheme.colorScheme.surfaceVariant,
      shape = MaterialTheme.shapes.small) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)) {
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
