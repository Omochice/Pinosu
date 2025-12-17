package io.github.omochice.pinosu.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.R
import io.github.omochice.pinosu.presentation.viewmodel.LoginUiState

/**
 * Composable function for the login screen
 *
 * Task 8.1: Basic implementation of LoginScreen
 * - Observe LoginViewModel.uiState with collectAsState()
 * - Place "Login with Amber" button
 * - Loading indicator display logic
 *
 * Task 8.2: Error dialog implementation
 * - Amber not installed dialog (with Play Store link)
 * - Timeout dialog (with retry button)
 * - Generic error dialog
 *
 * Task 8.3: Navigation on login success
 * - Display login success message
 * - Navigate to main screen
 *
 * Requirements: 3.1, 3.2, 3.3, 1.2, 1.5, 5.1, 5.4
 *
 * @param uiState Login screen UI state
 * @param onLoginButtonClick Callback when login button is clicked
 * @param onDismissDialog Callback to dismiss dialog
 * @param onInstallAmber Callback when Amber install button is clicked
 * @param onRetry Callback when retry button is clicked
 * @param onNavigateToMain Callback to navigate to main screen
 */
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onLoginButtonClick: () -> Unit,
    onDismissDialog: () -> Unit = {},
    onInstallAmber: () -> Unit = {},
    onRetry: () -> Unit = {},
    onNavigateToMain: () -> Unit = {}
) {
  // ========== Navigation on login success (Task 8.3) ==========
  // Automatically navigate to main screen when loginSuccess becomes true
  LaunchedEffect(uiState.loginSuccess) {
    if (uiState.loginSuccess) {
      onNavigateToMain()
    }
  }

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(16.dp)) {
            // Login success message (Task 8.3)
            if (uiState.loginSuccess) {
              Text(
                  text = stringResource(R.string.message_login_success),
                  style = MaterialTheme.typography.headlineSmall,
                  color = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading indicator (displayed only when isLoading is true)
            if (uiState.isLoading) {
              CircularProgressIndicator()
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                  text = stringResource(R.string.message_loading),
                  style = MaterialTheme.typography.bodyMedium)
              Spacer(modifier = Modifier.height(32.dp))
            }

            // Login button
            Button(
                onClick = onLoginButtonClick, enabled = !uiState.isLoading // Disabled while loading
                ) {
                  Text(stringResource(R.string.button_login_with_amber))
                }
          }
    }

    // ========== Error dialogs (Task 8.2) ==========

    // Amber not installed dialog
    if (uiState.showAmberInstallDialog) {
      AlertDialog(
          onDismissRequest = onDismissDialog,
          title = { Text(stringResource(R.string.dialog_title_amber_required)) },
          text = { Text(stringResource(R.string.dialog_message_amber_required)) },
          confirmButton = {
            Button(onClick = onInstallAmber) { Text(stringResource(R.string.button_install)) }
          },
          dismissButton = {
            TextButton(onClick = onDismissDialog) { Text(stringResource(R.string.button_close)) }
          })
    }

    // Generic error dialog (timeout, user rejection, other errors)
    if (uiState.errorMessage != null) {
      val isTimeoutError = uiState.errorMessage.contains("timeout", ignoreCase = true)

      AlertDialog(
          onDismissRequest = onDismissDialog,
          title = { Text(stringResource(R.string.dialog_title_error)) },
          text = { Text(uiState.errorMessage) },
          confirmButton = {
            if (isTimeoutError) {
              // Show retry button for timeout errors
              Button(onClick = onRetry) { Text(stringResource(R.string.button_retry)) }
            } else {
              // Show only close button for other errors
              TextButton(onClick = onDismissDialog) { Text(stringResource(R.string.button_ok)) }
            }
          },
          dismissButton = {
            if (isTimeoutError) {
              TextButton(onClick = onDismissDialog) { Text(stringResource(R.string.button_cancel)) }
            } else {
              null
            }
          })
    }
  }
}

/** LoginScreen preview - initial state */
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
  MaterialTheme { LoginScreen(uiState = LoginUiState(), onLoginButtonClick = {}) }
}

/** LoginScreen preview - loading state */
@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
  MaterialTheme { LoginScreen(uiState = LoginUiState(isLoading = true), onLoginButtonClick = {}) }
}

/** LoginScreen preview - Amber not installed dialog */
@Preview(showBackground = true)
@Composable
fun LoginScreenAmberInstallDialogPreview() {
  MaterialTheme {
    LoginScreen(uiState = LoginUiState(showAmberInstallDialog = true), onLoginButtonClick = {})
  }
}

/** LoginScreen preview - error dialog */
@Preview(showBackground = true)
@Composable
fun LoginScreenErrorDialogPreview() {
  MaterialTheme {
    LoginScreen(
        uiState = LoginUiState(errorMessage = "Login was cancelled. Please try again."),
        onLoginButtonClick = {})
  }
}

/** LoginScreen preview - timeout dialog */
@Preview(showBackground = true)
@Composable
fun LoginScreenTimeoutDialogPreview() {
  MaterialTheme {
    LoginScreen(
        uiState =
            LoginUiState(
                errorMessage = "Login process timed out. Please check the Amber app and retry."),
        onLoginButtonClick = {})
  }
}

/** LoginScreen preview - login success (Task 8.3) */
@Preview(showBackground = true)
@Composable
fun LoginScreenSuccessPreview() {
  MaterialTheme {
    LoginScreen(uiState = LoginUiState(loginSuccess = true), onLoginButtonClick = {})
  }
}
