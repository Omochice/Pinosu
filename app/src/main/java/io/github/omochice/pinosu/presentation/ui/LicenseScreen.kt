package io.github.omochice.pinosu.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import io.github.omochice.pinosu.R

/**
 * License screen displaying open source library licenses
 *
 * Uses AboutLibraries to automatically display all library licenses used in the app.
 *
 * @param onNavigateUp Callback when back navigation is triggered
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(onNavigateUp: () -> Unit) {
  val context = LocalContext.current
  val libraries by produceLibraries {
    context.resources.openRawResource(R.raw.aboutlibraries).bufferedReader().readText()
  }
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.title_licenses)) },
            navigationIcon = {
              IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate up")
              }
            })
      }) { paddingValues ->
        LibrariesContainer(libraries, modifier = Modifier.padding(paddingValues))
      }
}

@Preview(showBackground = true)
@Composable
private fun LicenseScreenPreview() {
  LicenseScreen(onNavigateUp = {})
}
