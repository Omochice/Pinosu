package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R

/**
 * Dialog where the user names the category to file a feed under, or takes it out of its category
 *
 * @param currentCategory Category the feed is in now, or null when it is in none
 * @param suggestions Names of categories already in use, offered so the user can reuse one
 * @param onSave Callback with the entered category, or null when the feed is to be in none
 * @param onDismiss Callback when the dialog is closed without changing the category
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedCategoryDialog(
    currentCategory: String?,
    suggestions: List<String>,
    onSave: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
  var input by rememberSaveable { mutableStateOf(currentCategory.orEmpty()) }
  val save = { onSave(input.ifBlank { null }) }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(stringResource(R.string.dialog_title_feed_category)) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
              value = input,
              onValueChange = { input = it },
              label = { Text(stringResource(R.string.label_feed_category)) },
              singleLine = true,
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
              keyboardActions = KeyboardActions(onDone = { save() }),
              modifier = Modifier.fillMaxWidth())
          FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.forEach { name ->
              SuggestionChip(onClick = { input = name }, label = { Text(name) })
            }
          }
        }
      },
      confirmButton = {
        Button(onClick = save) { Text(stringResource(R.string.button_save_feed_category)) }
      },
      dismissButton = {
        Row {
          if (currentCategory != null) {
            TextButton(onClick = { onSave(null) }) {
              Text(stringResource(R.string.button_clear_feed_category))
            }
          }
          TextButton(onClick = onDismiss) { Text(stringResource(R.string.button_cancel)) }
        }
      })
}
