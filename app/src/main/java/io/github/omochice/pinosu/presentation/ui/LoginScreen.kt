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
 * @param uiState Login screen UI state
 * @param onLoginButtonClick Callback when login button is clicked
 * @param onDismissDialog Callback to dismiss dialog
 * @param onInstallNip55Signer Callback when NIP-55 signer install button is clicked
 * @param onRetry Callback when retry button is clicked
 * @param onNavigateToMain Callback to navigate to main screen
 */
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onLoginButtonClick: () -> Unit,
    onDismissDialog: () -> Unit = {},
    onInstallNip55Signer: () -> Unit = {},
    onRetry: () -> Unit = {},
    onNavigateToMain: () -> Unit = {}
) {
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
            if (uiState.loginSuccess) {
              Text(
                  text = stringResource(R.string.message_login_success),
                  style = MaterialTheme.typography.headlineSmall,
                  color = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.isLoading) {
              CircularProgressIndicator()
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                  text = stringResource(R.string.message_loading),
                  style = MaterialTheme.typography.bodyMedium)
              Spacer(modifier = Modifier.height(32.dp))
            }

            Button(onClick = onLoginButtonClick, enabled = !uiState.isLoading) {
              Text(stringResource(R.string.button_login_with_nip55))
            }
          }
    }

    if (uiState.showNip55InstallDialog) {
      AlertDialog(
          onDismissRequest = onDismissDialog,
          title = { Text(stringResource(R.string.dialog_title_nip55_signer_required)) },
          text = { Text(stringResource(R.string.dialog_message_nip55_signer_required)) },
          confirmButton = {
            Button(onClick = onInstallNip55Signer) { Text(stringResource(R.string.button_install)) }
          },
          dismissButton = {
            TextButton(onClick = onDismissDialog) { Text(stringResource(R.string.button_close)) }
          })
    }

    if (uiState.errorMessage != null) {
      val isTimeoutError = uiState.errorMessage.contains("timeout", ignoreCase = true)

      AlertDialog(
          onDismissRequest = onDismissDialog,
          title = { Text(stringResource(R.string.dialog_title_error)) },
          text = { Text(uiState.errorMessage) },
          confirmButton = {
            if (isTimeoutError) {
              Button(onClick = onRetry) { Text(stringResource(R.string.button_retry)) }
            } else {
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

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
  MaterialTheme { LoginScreen(uiState = LoginUiState(), onLoginButtonClick = {}) }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
  MaterialTheme { LoginScreen(uiState = LoginUiState(isLoading = true), onLoginButtonClick = {}) }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenNip55InstallDialogPreview() {
  MaterialTheme {
    LoginScreen(uiState = LoginUiState(showNip55InstallDialog = true), onLoginButtonClick = {})
  }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenErrorDialogPreview() {
  MaterialTheme {
    LoginScreen(
        uiState = LoginUiState(errorMessage = "Login was cancelled. Please try again."),
        onLoginButtonClick = {})
  }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenTimeoutDialogPreview() {
  MaterialTheme {
    LoginScreen(
        uiState =
            LoginUiState(
                errorMessage =
                    "Login process timed out. Please check the NIP-55 signer app and retry."),
        onLoginButtonClick = {})
  }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenSuccessPreview() {
  MaterialTheme {
    LoginScreen(uiState = LoginUiState(loginSuccess = true), onLoginButtonClick = {})
  }
}
