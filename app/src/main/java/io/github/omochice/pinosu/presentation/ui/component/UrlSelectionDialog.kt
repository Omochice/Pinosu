package io.github.omochice.pinosu.presentation.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R

/**
 * Dialog for selecting URL from multiple URLs
 *
 * @param urls List of URLs to select from
 * @param onUrlSelected Callback when URL is selected
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
internal fun UrlSelectionDialog(
    urls: List<String>,
    onUrlSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
  var selectedUrl by remember { mutableStateOf(urls.firstOrNull()) }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(stringResource(R.string.dialog_title_select_url)) },
      text = {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
          urls.forEach { url ->
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .clickable { selectedUrl = url }
                        .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                  RadioButton(selected = selectedUrl == url, onClick = { selectedUrl = url })
                  Text(
                      text = url,
                      modifier = Modifier.padding(start = 8.dp),
                      style = MaterialTheme.typography.bodyMedium,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis)
                }
          }
        }
      },
      confirmButton = {
        Button(onClick = { selectedUrl?.let(onUrlSelected) }, enabled = selectedUrl != null) {
          Text(stringResource(R.string.button_open))
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.button_cancel)) }
      })
}
