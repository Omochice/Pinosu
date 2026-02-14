package io.github.omochice.pinosu.feature.appinfo.presentation.ui

import android.content.ClipData
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.appinfo.presentation.model.AppInfoUiState

/**
 * Application information screen
 *
 * Displays app version with commit hash and provides a copy-to-clipboard button.
 *
 * @param uiState UI state containing version and commit hash information
 * @param onNavigateUp Callback when back navigation is triggered
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoScreen(uiState: AppInfoUiState, onNavigateUp: () -> Unit) {
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current
  val appIcon =
      remember(context) {
        val drawable = context.packageManager.getApplicationIcon(context.applicationInfo)
        drawableToBitmap(drawable).asImageBitmap()
      }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.title_app_info)) },
            navigationIcon = {
              IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate up")
              }
            })
      }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp)) {
          Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Image(
                bitmap = appIcon,
                contentDescription = null,
                modifier = Modifier.size(96.dp).clip(RoundedCornerShape(16.dp)))
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
              text = stringResource(R.string.app_name),
              style = MaterialTheme.typography.headlineMedium)

          Spacer(modifier = Modifier.height(16.dp))

          Text(
              text = stringResource(R.string.label_version),
              style = MaterialTheme.typography.titleMedium)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = uiState.versionDisplayText, style = MaterialTheme.typography.bodyLarge)
            IconButton(
                onClick = {
                  clipboardManager.setClip(
                      ClipEntry(ClipData.newPlainText("version", uiState.versionDisplayText)))
                }) {
                  Icon(
                      imageVector = Icons.Default.ContentCopy,
                      contentDescription = stringResource(R.string.cd_copy_version))
                }
          }
        }
      }
}

private fun drawableToBitmap(drawable: android.graphics.drawable.Drawable): Bitmap {
  val bitmap =
      Bitmap.createBitmap(
          drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(bitmap)
  drawable.setBounds(0, 0, canvas.width, canvas.height)
  drawable.draw(canvas)
  return bitmap
}

@Preview(showBackground = true)
@Composable
private fun AppInfoScreenPreview() {
  AppInfoScreen(
      uiState = AppInfoUiState(versionName = "0.3.0", commitHash = "abc1234"), onNavigateUp = {})
}
