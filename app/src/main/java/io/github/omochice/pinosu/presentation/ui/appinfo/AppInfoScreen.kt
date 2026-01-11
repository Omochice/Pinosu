package io.github.omochice.pinosu.presentation.ui.appinfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R

/**
 * Application information screen
 *
 * Displays app version and other metadata.
 *
 * @param onNavigateUp Callback when back navigation is triggered
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoScreen(onNavigateUp: () -> Unit) {
  val context = LocalContext.current
  val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
  val versionName = packageInfo.versionName

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
          Text(
              text = stringResource(R.string.app_name),
              style = MaterialTheme.typography.headlineMedium)

          Spacer(modifier = Modifier.height(16.dp))

          Text(
              text = stringResource(R.string.label_version),
              style = MaterialTheme.typography.titleMedium)
          Text(text = versionName ?: "Unknown", style = MaterialTheme.typography.bodyLarge)
        }
      }
}

@Preview(showBackground = true)
@Composable
private fun AppInfoScreenPreview() {
  AppInfoScreen(onNavigateUp = {})
}
