package io.github.omochice.pinosu.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import io.github.omochice.pinosu.presentation.viewmodel.MainUiState

/**
 * Main screen UI
 *
 * Task 9.1: Basic implementation of MainScreen
 * - Place logout button
 * - Display user pubkey (with partial masking)
 * - Show loading indicator during logout process
 *
 * Task 9.2: Logout process and navigation
 * - Navigate to login screen after logout completes
 *
 * Requirements: 2.3, 2.4, 3.4, 3.5
 *
 * @param uiState Main screen UI state
 * @param onLogout Callback when logout button is tapped
 * @param onNavigateToLogin Callback to navigate to login screen after logout completes (Task 9.2)
 */
@Composable
fun MainScreen(
    uiState: MainUiState,
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
) {
  // Task 9.2: Detect logout completion - navigate to login screen when pubkey becomes null
  // Remember previous pubkey state and detect transition from logged in to logged out (null)
  var previousPubkey by remember { mutableStateOf(uiState.userPubkey) }

  LaunchedEffect(uiState.userPubkey) {
    // Navigate when transitioning from logged in (previousPubkey != null) to logged out (userPubkey
    // == null)
    if (previousPubkey != null && uiState.userPubkey == null && !uiState.isLoggingOut) {
      onNavigateToLogin()
    }
    previousPubkey = uiState.userPubkey
  }

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        // Display login status
        if (uiState.userPubkey != null) {
          // Display user public key (Requirement 3.5: Display logged-in pubkey on main screen)
          Text(
              text = stringResource(R.string.text_logged_in),
              style = MaterialTheme.typography.headlineMedium,
              textAlign = TextAlign.Center)

          Spacer(modifier = Modifier.height(16.dp))

          // Display pubkey (with partial masking recommended)
          Text(
              text = formatPubkey(uiState.userPubkey),
              style = MaterialTheme.typography.bodyMedium,
              textAlign = TextAlign.Center)

          Spacer(modifier = Modifier.height(32.dp))

          // Logout button or logging out message (Requirement 3.4: Place logout button on main
          // screen)
          if (uiState.isLoggingOut) {
            // Display loading message during logout process (Requirement 3.2: Loading indicator)
            Text(
                text = stringResource(R.string.message_logging_out),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center)
          } else {
            // Logout button
            Button(onClick = onLogout) { Text(stringResource(R.string.button_logout)) }
          }
        } else {
          // Not logged in state (Requirement 2.3: Check logged-in status)
          Text(
              text = stringResource(R.string.text_not_logged_in),
              style = MaterialTheme.typography.headlineMedium,
              textAlign = TextAlign.Center)
        }
      }
}

/**
 * Format public key for display
 *
 * According to design.md L329, pubkey should be displayed with partial masking: show first 8
 * characters and last 8 characters, omit the middle
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

// ========== Previews ==========

/** MainScreen preview - logged in state */
@Preview(showBackground = true)
@Composable
private fun MainScreenLoggedInPreview() {
  val uiState =
      MainUiState(
          userPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
          isLoggingOut = false)
  MainScreen(uiState = uiState, onLogout = {})
}

/** MainScreen preview - logging out state */
@Preview(showBackground = true)
@Composable
private fun MainScreenLoggingOutPreview() {
  val uiState =
      MainUiState(
          userPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
          isLoggingOut = true)
  MainScreen(uiState = uiState, onLogout = {})
}

/** MainScreen preview - not logged in state */
@Preview(showBackground = true)
@Composable
private fun MainScreenNotLoggedInPreview() {
  val uiState = MainUiState(userPubkey = null, isLoggingOut = false)
  MainScreen(uiState = uiState, onLogout = {})
}
