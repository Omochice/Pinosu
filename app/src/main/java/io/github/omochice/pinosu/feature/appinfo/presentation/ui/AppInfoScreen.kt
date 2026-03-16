package io.github.omochice.pinosu.feature.appinfo.presentation.ui

import android.content.ClipData
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.appinfo.presentation.model.AppInfoUiState
import kotlinx.coroutines.launch

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
  val uriHandler = LocalUriHandler.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val urlOpenFailedMessage = stringResource(R.string.error_url_open_failed)
  val appIcon = rememberAppIcon()

  Scaffold(
      snackbarHost = { SnackbarHost(snackbarHostState) },
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

          VersionSection(
              versionDisplayText = uiState.versionDisplayText,
              onCopyVersion = {
                clipboardManager.setClip(
                    ClipEntry(ClipData.newPlainText("version", uiState.versionDisplayText)))
              })

          Spacer(modifier = Modifier.height(16.dp))

          val repositoryUrl = stringResource(R.string.url_repository)
          RepositorySection(
              repositoryUrl = repositoryUrl,
              onOpenUrl = {
                try {
                  uriHandler.openUri(repositoryUrl)
                } catch (_: Exception) {
                  scope.launch { snackbarHostState.showSnackbar(urlOpenFailedMessage) }
                }
              })
        }
      }
}

/** Displays the version label, version text, and a copy-to-clipboard button. */
@Composable
private fun VersionSection(versionDisplayText: String, onCopyVersion: () -> Unit) {
  Text(text = stringResource(R.string.label_version), style = MaterialTheme.typography.titleMedium)
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(text = versionDisplayText, style = MaterialTheme.typography.bodyLarge)
    IconButton(onClick = onCopyVersion) {
      Icon(
          imageVector = Icons.Default.ContentCopy,
          contentDescription = stringResource(R.string.cd_copy_version))
    }
  }
}

/** Displays the repository label and a clickable URL that opens in the browser. */
@Composable
private fun RepositorySection(repositoryUrl: String, onOpenUrl: () -> Unit) {
  Text(
      text = stringResource(R.string.label_repository),
      style = MaterialTheme.typography.titleMedium)
  Text(
      text = repositoryUrl,
      style =
          MaterialTheme.typography.bodyLarge.copy(
              color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline),
      modifier = Modifier.clickable { onOpenUrl() })
}

/** Remembers the application icon bitmap, handling both preview and runtime contexts. */
@Composable
private fun rememberAppIcon(): ImageBitmap {
  val context = LocalContext.current
  val isInPreview = LocalInspectionMode.current
  val density = LocalDensity.current.density
  return remember(context, density) {
    if (isInPreview) {
      createBitmap(1, 1).asImageBitmap()
    } else {
      val drawable = context.packageManager.getApplicationIcon(context.applicationInfo)
      adaptiveIconToBitmap(drawable, density).asImageBitmap()
    }
  }
}

/**
 * Renders a Drawable to a Bitmap without platform launcher mask applied.
 *
 * For AdaptiveIconDrawable, draws background and foreground layers directly so the Compose-side
 * RoundedCornerShape clip determines the final shape instead of the system's circular/squircle
 * mask.
 */
private const val ADAPTIVE_ICON_SIZE_DP = 108

private fun adaptiveIconToBitmap(
    drawable: android.graphics.drawable.Drawable,
    density: Float
): Bitmap {
  val size = (ADAPTIVE_ICON_SIZE_DP * density).toInt()
  val bitmap = createBitmap(size, size)
  val canvas = Canvas(bitmap)
  if (drawable is android.graphics.drawable.AdaptiveIconDrawable) {
    drawable.background?.let { bg ->
      bg.setBounds(0, 0, size, size)
      bg.draw(canvas)
    }
    drawable.foreground?.let { fg ->
      fg.setBounds(0, 0, size, size)
      fg.draw(canvas)
    }
  } else {
    drawable.setBounds(0, 0, size, size)
    drawable.draw(canvas)
  }
  return bitmap
}

@Preview(showBackground = true)
@Composable
private fun AppInfoScreenPreview() {
  AppInfoScreen(
      uiState = AppInfoUiState(versionName = "0.3.0", commitHash = "abc1234"), onNavigateUp = {})
}
