package io.github.omochice.pinosu.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.domain.model.ThemeMode
import io.github.omochice.pinosu.presentation.viewmodel.SettingsUiState

/**
 * Settings screen
 *
 * Displays app settings including theme mode selection.
 *
 * @param uiState Current settings UI state
 * @param onThemeModeSelected Callback when theme mode is selected
 * @param onNavigateUp Callback when back navigation is triggered
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onNavigateUp: () -> Unit
) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.title_settings)) },
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
              text = stringResource(R.string.label_theme),
              style = MaterialTheme.typography.titleMedium)

          Spacer(modifier = Modifier.height(8.dp))

          ThemeMode.entries.forEach { mode ->
            ThemeModeRadioItem(
                mode = mode,
                selected = uiState.themeMode == mode,
                onClick = { onThemeModeSelected(mode) })
          }
        }
      }
}

@Composable
private fun ThemeModeRadioItem(mode: ThemeMode, selected: Boolean, onClick: () -> Unit) {
  Row(
      modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text =
                stringResource(
                    when (mode) {
                      ThemeMode.Light -> R.string.theme_light
                      ThemeMode.Dark -> R.string.theme_dark
                      ThemeMode.System -> R.string.theme_system
                    }),
            style = MaterialTheme.typography.bodyLarge)
      }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
  SettingsScreen(
      uiState = SettingsUiState(themeMode = ThemeMode.System),
      onThemeModeSelected = {},
      onNavigateUp = {})
}
