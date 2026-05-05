package io.github.omochice.pinosu.feature.comment.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.comment.domain.model.Comment

/**
 * Card for displaying a NIP-22 kind 1111 comment
 *
 * Long-press shows a context menu to copy the comment content or raw event JSON.
 *
 * @param comment The comment to display
 * @param onCopyContent Called when the user selects "Copy content"
 * @param profileImageUrl Profile image URL for the comment author, or null for fallback icon
 * @param onCopyRawJson Called when the user selects "Copy raw JSON", or null to hide the option
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CommentCard(
    comment: Comment,
    onCopyContent: (String) -> Unit,
    profileImageUrl: String? = null,
    onCopyRawJson: (() -> Unit)? = null,
) {
  var showMenu by remember { mutableStateOf(false) }

  Box {
    Card(
        modifier =
            Modifier.fillMaxWidth()
                .combinedClickable(onClick = {}, onLongClick = { showMenu = true }),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
          Column(modifier = Modifier.padding(12.dp)) {
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

    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
      DropdownMenuItem(
          text = { Text(stringResource(R.string.menu_copy_content)) },
          onClick = {
            onCopyContent(comment.content)
            showMenu = false
          })
      if (onCopyRawJson != null) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_copy_raw_json)) },
            onClick = {
              onCopyRawJson()
              showMenu = false
            })
      }
    }
  }
}
