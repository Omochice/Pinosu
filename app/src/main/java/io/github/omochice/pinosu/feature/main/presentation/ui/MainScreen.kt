package io.github.omochice.pinosu.feature.main.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.feature.auth.presentation.viewmodel.MainUiState

/**
 * Main screen UI
 *
 * @param uiState Main screen UI state
 * @param onLogout Callback when logout button is tapped
 * @param onOpenDrawer Callback when hamburger menu is clicked to open drawer
 * @param onNavigateToLogin Callback to navigate to login screen after logout completes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onLogout: () -> Unit,
    onOpenDrawer: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
) {
  var previousPubkey by remember { mutableStateOf(uiState.userPubkey) }

  LaunchedEffect(uiState.userPubkey) {
    if (previousPubkey != null && uiState.userPubkey == null && !uiState.isLoggingOut) {
      onNavigateToLogin()
    }
    previousPubkey = uiState.userPubkey
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            navigationIcon = {
              IconButton(onClick = onOpenDrawer) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Open menu")
              }
            })
      }) { paddingValues ->
        Surface(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            color = MaterialTheme.colorScheme.background) {
              Column(
                  modifier = Modifier.fillMaxSize().padding(16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center) {
                    if (uiState.userPubkey != null) {
                      Text(
                          text = stringResource(R.string.text_logged_in),
                          style = MaterialTheme.typography.headlineMedium,
                          textAlign = TextAlign.Center)

                      Spacer(modifier = Modifier.height(16.dp))

                      Text(
                          text = formatPubkey(uiState.userPubkey),
                          style = MaterialTheme.typography.bodyMedium,
                          textAlign = TextAlign.Center)

                      Spacer(modifier = Modifier.height(32.dp))

                      if (uiState.isLoggingOut) {
                        Text(
                            text = stringResource(R.string.message_logging_out),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center)
                      } else {
                        Button(onClick = onLogout) { Text(stringResource(R.string.button_logout)) }
                      }
                    } else {
                      Text(
                          text = stringResource(R.string.text_not_logged_in),
                          style = MaterialTheme.typography.headlineMedium,
                          textAlign = TextAlign.Center)
                    }
                  }
            }
      }
}

/**
 * Format public key for display
 *
 * @param pubkey Nostr public key (64-character hexadecimal)
 * @return Formatted public key string
 */
private fun formatPubkey(pubkey: String): String {
  return if (pubkey.length >= 16) {
    "${pubkey.take(8)}...${pubkey.takeLast(8)}"
  } else {
    pubkey
  }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenLoggedInPreview() {
  val uiState =
      MainUiState(
          userPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
          isLoggingOut = false)
  MainScreen(uiState = uiState, onLogout = {})
}

@Preview(showBackground = true)
@Composable
private fun MainScreenLoggingOutPreview() {
  val uiState =
      MainUiState(
          userPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
          isLoggingOut = true)
  MainScreen(uiState = uiState, onLogout = {})
}

@Preview(showBackground = true)
@Composable
private fun MainScreenNotLoggedInPreview() {
  val uiState = MainUiState(userPubkey = null, isLoggingOut = false)
  MainScreen(uiState = uiState, onLogout = {})
}
