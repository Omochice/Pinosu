package io.github.omochice.pinosu.feature.settings.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.bookmark.domain.model.BookmarkDisplayMode
import io.github.omochice.pinosu.feature.settings.domain.model.LanguageMode
import io.github.omochice.pinosu.feature.settings.domain.model.ThemeMode
import io.github.omochice.pinosu.feature.settings.presentation.viewmodel.SettingsUiState

/**
 * Settings screen
 *
 * Allows users to configure app preferences.
 *
 * @param uiState Current settings UI state
 * @param onNavigateUp Callback when back navigation is triggered
 * @param onDisplayModeChange Callback when display mode is changed
 * @param onThemeModeChange Callback when theme mode is changed
 * @param onLanguageModeChange Callback when language mode is changed
 * @param onAddBootstrapRelay Callback when a bootstrap relay URL is added
 * @param onRemoveBootstrapRelay Callback when a bootstrap relay URL is removed
 * @param onResetBootstrapRelays Callback when bootstrap relays are reset to defaults
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateUp: () -> Unit,
    onDisplayModeChange: (BookmarkDisplayMode) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageModeChange: (LanguageMode) -> Unit,
    onAddBootstrapRelay: (String) -> Unit = {},
    onRemoveBootstrapRelay: (String) -> Unit = {},
    onResetBootstrapRelays: () -> Unit = {},
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
        Column(
            modifier =
                Modifier.padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())) {
              DisplayModeSection(
                  displayMode = uiState.displayMode, onDisplayModeChange = onDisplayModeChange)

              Spacer(modifier = Modifier.height(24.dp))

              ThemeModeSection(themeMode = uiState.themeMode, onThemeModeChange = onThemeModeChange)

              Spacer(modifier = Modifier.height(24.dp))

              LanguageModeSection(
                  languageMode = uiState.languageMode, onLanguageModeChange = onLanguageModeChange)

              Spacer(modifier = Modifier.height(24.dp))

              BootstrapRelaysSection(
                  relays = uiState.bootstrapRelays,
                  onAddRelay = onAddBootstrapRelay,
                  onRemoveRelay = onRemoveBootstrapRelay,
                  onResetDefaults = onResetBootstrapRelays)
            }
      }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DisplayModeSection(
    displayMode: BookmarkDisplayMode,
    onDisplayModeChange: (BookmarkDisplayMode) -> Unit,
) {
  Text(
      text = stringResource(R.string.settings_display_mode),
      style = MaterialTheme.typography.titleMedium)

  Spacer(modifier = Modifier.height(8.dp))

  FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    FilterChip(
        selected = displayMode == BookmarkDisplayMode.List,
        onClick = { onDisplayModeChange(BookmarkDisplayMode.List) },
        label = { Text(stringResource(R.string.display_mode_list)) })

    FilterChip(
        selected = displayMode == BookmarkDisplayMode.Grid,
        onClick = { onDisplayModeChange(BookmarkDisplayMode.Grid) },
        label = { Text(stringResource(R.string.display_mode_grid)) })
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeModeSection(themeMode: ThemeMode, onThemeModeChange: (ThemeMode) -> Unit) {
  Text(
      text = stringResource(R.string.settings_theme_mode),
      style = MaterialTheme.typography.titleMedium)

  Spacer(modifier = Modifier.height(8.dp))

  FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    FilterChip(
        selected = themeMode == ThemeMode.System,
        onClick = { onThemeModeChange(ThemeMode.System) },
        label = { Text(stringResource(R.string.theme_mode_system)) })

    FilterChip(
        selected = themeMode == ThemeMode.Light,
        onClick = { onThemeModeChange(ThemeMode.Light) },
        label = { Text(stringResource(R.string.theme_mode_light)) })

    FilterChip(
        selected = themeMode == ThemeMode.Dark,
        onClick = { onThemeModeChange(ThemeMode.Dark) },
        label = { Text(stringResource(R.string.theme_mode_dark)) })
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LanguageModeSection(
    languageMode: LanguageMode,
    onLanguageModeChange: (LanguageMode) -> Unit,
) {
  Text(
      text = stringResource(R.string.settings_language_mode),
      style = MaterialTheme.typography.titleMedium)

  Spacer(modifier = Modifier.height(8.dp))

  FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    FilterChip(
        selected = languageMode == LanguageMode.System,
        onClick = { onLanguageModeChange(LanguageMode.System) },
        label = { Text(stringResource(R.string.language_mode_system)) })

    FilterChip(
        selected = languageMode == LanguageMode.English,
        onClick = { onLanguageModeChange(LanguageMode.English) },
        label = { Text(stringResource(R.string.language_mode_english)) })

    FilterChip(
        selected = languageMode == LanguageMode.Japanese,
        onClick = { onLanguageModeChange(LanguageMode.Japanese) },
        label = { Text(stringResource(R.string.language_mode_japanese)) })
  }
}

@Composable
private fun BootstrapRelaysSection(
    relays: Set<String>,
    onAddRelay: (String) -> Unit,
    onRemoveRelay: (String) -> Unit,
    onResetDefaults: () -> Unit,
) {
  Text(
      text = stringResource(R.string.settings_bootstrap_relays),
      style = MaterialTheme.typography.titleMedium)

  Spacer(modifier = Modifier.height(4.dp))

  Text(
      text = stringResource(R.string.settings_bootstrap_relays_description),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant)

  Spacer(modifier = Modifier.height(8.dp))

  relays.sorted().forEach { url ->
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = url,
              modifier = Modifier.weight(1f),
              style = MaterialTheme.typography.bodyMedium)
          IconButton(onClick = { onRemoveRelay(url) }) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_remove_relay))
          }
        }
  }

  var newRelayUrl by rememberSaveable { mutableStateOf("") }
  val trimmedUrl = newRelayUrl.trim()
  val isUrlValid = trimmedUrl.startsWith("wss://") || trimmedUrl.startsWith("ws://")

  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    OutlinedTextField(
        value = newRelayUrl,
        onValueChange = { newRelayUrl = it },
        modifier = Modifier.weight(1f),
        placeholder = { Text(stringResource(R.string.hint_relay_url)) },
        singleLine = true)

    Spacer(modifier = Modifier.width(8.dp))

    OutlinedButton(
        onClick = {
          onAddRelay(trimmedUrl)
          newRelayUrl = ""
        },
        enabled = isUrlValid) {
          Text(stringResource(R.string.button_add_relay))
        }
  }

  Spacer(modifier = Modifier.height(8.dp))

  TextButton(onClick = onResetDefaults) { Text(stringResource(R.string.button_reset_defaults)) }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
  SettingsScreen(
      uiState = SettingsUiState(displayMode = BookmarkDisplayMode.List),
      onNavigateUp = {},
      onDisplayModeChange = {},
      onThemeModeChange = {},
      onLanguageModeChange = {})
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenGridPreview() {
  SettingsScreen(
      uiState = SettingsUiState(displayMode = BookmarkDisplayMode.Grid),
      onNavigateUp = {},
      onDisplayModeChange = {},
      onThemeModeChange = {},
      onLanguageModeChange = {})
}
