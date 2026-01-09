package io.github.omochice.pinosu.presentation.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.omochice.pinosu.R

/**
 * Dialog for displaying error message
 *
 * @param message Error message to display
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
internal fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit,
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(stringResource(R.string.dialog_title_error)) },
      text = { Text(message) },
      confirmButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.button_ok)) } })
}
