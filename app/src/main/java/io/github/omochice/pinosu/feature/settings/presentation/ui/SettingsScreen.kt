package io.github.omochice.pinosu.feature.settings.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.presentation.viewmodel.SettingsUiState

/**
 * Settings screen
 *
 * Allows users to configure app preferences.
 *
 * @param uiState Current settings UI state
 * @param onNavigateUp Callback when back navigation is triggered
 * @param onDisplayModeChange Callback when display mode is changed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateUp: () -> Unit,
    onDisplayModeChange: (BookmarkDisplayMode) -> Unit,
) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.title_settings)) },
            navigationIcon = {
              IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_navigate_up))
              }
            })
      }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp)) {
          Text(
              text = stringResource(R.string.settings_display_mode),
              style = MaterialTheme.typography.titleMedium)

          Spacer(modifier = Modifier.height(8.dp))

          Row {
            FilterChip(
                selected = uiState.displayMode == BookmarkDisplayMode.List,
                onClick = { onDisplayModeChange(BookmarkDisplayMode.List) },
                label = { Text(stringResource(R.string.display_mode_list)) })

            Spacer(modifier = Modifier.width(8.dp))

            FilterChip(
                selected = uiState.displayMode == BookmarkDisplayMode.Grid,
                onClick = { onDisplayModeChange(BookmarkDisplayMode.Grid) },
                label = { Text(stringResource(R.string.display_mode_grid)) })
          }
        }
      }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
  SettingsScreen(
      uiState = SettingsUiState(displayMode = BookmarkDisplayMode.List),
      onNavigateUp = {},
      onDisplayModeChange = {})
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenGridPreview() {
  SettingsScreen(
      uiState = SettingsUiState(displayMode = BookmarkDisplayMode.Grid),
      onNavigateUp = {},
      onDisplayModeChange = {})
}
