package io.github.omochice.pinosu.feature.auth.presentation.ui

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
import io.github.omochice.pinosu.feature.auth.presentation.viewmodel.LoginUiState

/**
 * Composable function for the login screen
 *
 * @param uiState Login screen UI state
 * @param onLoginButtonClick Callback when login button is clicked
 * @param onDismissDialog Callback to dismiss dialog
 * @param onInstallNip55Signer Callback when NIP-55 signer install button is clicked
 * @param onRetry Callback when retry button is clicked
 * @param onLoginSuccess Callback when login succeeds
 */
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onLoginButtonClick: () -> Unit,
    onDismissDialog: () -> Unit = {},
    onInstallNip55Signer: () -> Unit = {},
    onRetry: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
  val isSuccess = uiState is LoginUiState.Success
  LaunchedEffect(isSuccess) {
    if (isSuccess) {
      onLoginSuccess()
    }
  }

  val isLoading = uiState is LoginUiState.Loading

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(16.dp)) {
            if (isSuccess) {
              Text(
                  text = stringResource(R.string.message_login_success),
                  style = MaterialTheme.typography.headlineSmall,
                  color = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.height(16.dp))
            }

            if (isLoading) {
              CircularProgressIndicator()
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                  text = stringResource(R.string.message_loading),
                  style = MaterialTheme.typography.bodyMedium)
              Spacer(modifier = Modifier.height(32.dp))
            }

            Button(onClick = onLoginButtonClick, enabled = !isLoading) {
              Text(stringResource(R.string.button_login_with_nip55))
            }
          }
    }

    when (uiState) {
      LoginUiState.Idle,
      LoginUiState.Loading,
      LoginUiState.Success -> {}
      LoginUiState.RequiresNip55Install -> {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text(stringResource(R.string.dialog_title_nip55_signer_required)) },
            text = { Text(stringResource(R.string.dialog_message_nip55_signer_required)) },
            confirmButton = {
              Button(onClick = onInstallNip55Signer) {
                Text(stringResource(R.string.button_install))
              }
            },
            dismissButton = {
              TextButton(onClick = onDismissDialog) { Text(stringResource(R.string.button_close)) }
            })
      }
      is LoginUiState.Error.Retryable -> {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text(stringResource(R.string.dialog_title_error)) },
            text = { Text(uiState.message) },
            confirmButton = {
              Button(onClick = onRetry) { Text(stringResource(R.string.button_retry)) }
            },
            dismissButton = {
              TextButton(onClick = onDismissDialog) { Text(stringResource(R.string.button_cancel)) }
            })
      }
      is LoginUiState.Error.NonRetryable -> {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text(stringResource(R.string.dialog_title_error)) },
            text = { Text(uiState.message) },
            confirmButton = {
              TextButton(onClick = onDismissDialog) { Text(stringResource(R.string.button_ok)) }
            })
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
  MaterialTheme { LoginScreen(uiState = LoginUiState.Idle, onLoginButtonClick = {}) }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
  MaterialTheme { LoginScreen(uiState = LoginUiState.Loading, onLoginButtonClick = {}) }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenNip55InstallDialogPreview() {
  MaterialTheme {
    LoginScreen(uiState = LoginUiState.RequiresNip55Install, onLoginButtonClick = {})
  }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenErrorDialogPreview() {
  MaterialTheme {
    LoginScreen(
        uiState = LoginUiState.Error.NonRetryable("Login was cancelled. Please try again."),
        onLoginButtonClick = {})
  }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenTimeoutDialogPreview() {
  MaterialTheme {
    LoginScreen(
        uiState =
            LoginUiState.Error.Retryable(
                "Login process timed out. Please check the NIP-55 signer app and retry."),
        onLoginButtonClick = {})
  }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenSuccessPreview() {
  MaterialTheme { LoginScreen(uiState = LoginUiState.Success, onLoginButtonClick = {}) }
}
