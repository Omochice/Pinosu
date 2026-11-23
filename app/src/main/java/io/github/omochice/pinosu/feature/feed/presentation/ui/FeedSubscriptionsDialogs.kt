package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.feed.domain.model.FeedCandidate
import io.github.omochice.pinosu.feature.feed.presentation.viewmodel.FeedSubscriptionsDialog

/**
 * Dialog asking the user the question a change to the subscription list is waiting on
 *
 * @param dialog Question to ask, or null to show nothing
 * @param callbacks Callbacks that answer the question
 */
@Composable
fun FeedSubscriptionsDialogHost(
    dialog: FeedSubscriptionsDialog?,
    callbacks: FeedSubscriptionsCallbacks,
) {
  when (dialog) {
    is FeedSubscriptionsDialog.ChooseCandidate ->
        ChooseFeedDialog(
            candidates = dialog.candidates,
            onPick = callbacks.onAdd,
            onDismiss = callbacks.onDismissDialog)
    is FeedSubscriptionsDialog.ConfirmPublicList ->
        PublicFeedListDialog(
            onConfirm = callbacks.onConfirmPublicNotice, onDismiss = callbacks.onDismissDialog)
    null -> Unit
  }
}

/**
 * Dialog where the user enters a feed or website URL to subscribe to
 *
 * @param onSubmit Callback with the entered URL
 * @param onDismiss Callback when the dialog is closed without submitting
 */
@Composable
fun AddFeedDialog(onSubmit: (String) -> Unit, onDismiss: () -> Unit) {
  var input by rememberSaveable { mutableStateOf("") }
  val submit = { if (input.isNotBlank()) onSubmit(input.trim()) }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(stringResource(R.string.dialog_title_add_feed)) },
      text = {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(stringResource(R.string.label_feed_url)) },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.fillMaxWidth())
      },
      confirmButton = {
        Button(onClick = submit, enabled = input.isNotBlank()) {
          Text(stringResource(R.string.button_add_feed))
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.button_cancel)) }
      })
}

@Composable
private fun ChooseFeedDialog(
    candidates: List<FeedCandidate>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(stringResource(R.string.dialog_title_choose_feed)) },
      text = {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
          candidates.forEach { candidate ->
            ListItem(
                headlineContent = { Text(candidate.title ?: candidate.url) },
                supportingContent =
                    candidate.title?.let {
                      { Text(candidate.url, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    },
                modifier = Modifier.clickable { onPick(candidate.url) })
          }
        }
      },
      confirmButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.button_cancel)) }
      })
}

@Composable
private fun PublicFeedListDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(stringResource(R.string.dialog_title_feed_list_public)) },
      text = { Text(stringResource(R.string.dialog_message_feed_list_public)) },
      confirmButton = {
        Button(onClick = onConfirm) { Text(stringResource(R.string.button_publish_feed_list)) }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.button_cancel)) }
      })
}
