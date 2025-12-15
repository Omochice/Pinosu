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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.presentation.viewmodel.LoginUiState

/**
 * ログイン画面のComposable関数
 *
 * Task 8.1: LoginScreenの基本実装
 * - LoginViewModel.uiStateのcollectAsState()観察
 * - 「Amberでログイン」ボタンの配置
 * - ローディングインジケーターの表示ロジック
 *
 * Task 8.2: エラーダイアログの実装
 * - Amber未インストールダイアログ（Play Storeリンク付き）
 * - タイムアウトダイアログ（再試行ボタン付き）
 * - 汎用エラーダイアログ
 *
 * Task 8.3: ログイン成功時のナビゲーション
 * - ログイン成功メッセージの表示
 * - メイン画面への画面遷移
 *
 * Requirements: 3.1, 3.2, 3.3, 1.2, 1.5, 5.1, 5.4
 *
 * @param uiState ログイン画面のUI状態
 * @param onLoginButtonClick ログインボタンクリック時のコールバック
 * @param onDismissDialog ダイアログを閉じる時のコールバック
 * @param onInstallAmber Amberインストールボタンクリック時のコールバック
 * @param onRetry 再試行ボタンクリック時のコールバック
 * @param onNavigateToMain メイン画面への遷移コールバック
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
  // ========== ログイン成功時のナビゲーション (Task 8.3) ==========
  // loginSuccessがtrueになったら自動的にメイン画面に遷移する
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
            // ログイン成功メッセージ (Task 8.3)
            if (uiState.loginSuccess) {
              Text(
                  text = "ログインに成功しました",
                  style = MaterialTheme.typography.headlineSmall,
                  color = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.height(16.dp))
            }

            // ローディングインジケーター（isLoadingがtrueの時のみ表示）
            if (uiState.isLoading) {
              CircularProgressIndicator()
              Spacer(modifier = Modifier.height(16.dp))
              Text(text = "読み込み中...", style = MaterialTheme.typography.bodyMedium)
              Spacer(modifier = Modifier.height(32.dp))
            }

            // ログインボタン
            Button(
                onClick = onLoginButtonClick, enabled = !uiState.isLoading // ローディング中は無効化
                ) {
                  Text("Amberでログイン")
                }
          }
    }

    // ========== エラーダイアログ (Task 8.2) ==========

    // Amber未インストールダイアログ
    if (uiState.showAmberInstallDialog) {
      AlertDialog(
          onDismissRequest = onDismissDialog,
          title = { Text("Amberアプリが必要です") },
          text = { Text("このアプリを使用するにはAmberアプリのインストールが必要です。") },
          confirmButton = { Button(onClick = onInstallAmber) { Text("インストール") } },
          dismissButton = { TextButton(onClick = onDismissDialog) { Text("閉じる") } })
    }

    // 汎用エラーダイアログ（タイムアウト、ユーザー拒否、その他エラー）
    if (uiState.errorMessage != null) {
      val isTimeoutError = uiState.errorMessage.contains("タイムアウト")

      AlertDialog(
          onDismissRequest = onDismissDialog,
          title = { Text("エラー") },
          text = { Text(uiState.errorMessage) },
          confirmButton = {
            if (isTimeoutError) {
              // タイムアウトエラーの場合は再試行ボタンを表示
              Button(onClick = onRetry) { Text("再試行") }
            } else {
              // その他のエラーの場合は閉じるボタンのみ
              TextButton(onClick = onDismissDialog) { Text("OK") }
            }
          },
          dismissButton = {
            if (isTimeoutError) {
              TextButton(onClick = onDismissDialog) { Text("キャンセル") }
            } else {
              null
            }
          })
    }
  }
}

/** LoginScreenのプレビュー - 初期状態 */
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
  MaterialTheme { LoginScreen(uiState = LoginUiState(), onLoginButtonClick = {}) }
}

/** LoginScreenのプレビュー - ローディング中 */
@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
  MaterialTheme { LoginScreen(uiState = LoginUiState(isLoading = true), onLoginButtonClick = {}) }
}

/** LoginScreenのプレビュー - Amber未インストールダイアログ */
@Preview(showBackground = true)
@Composable
fun LoginScreenAmberInstallDialogPreview() {
  MaterialTheme {
    LoginScreen(uiState = LoginUiState(showAmberInstallDialog = true), onLoginButtonClick = {})
  }
}

/** LoginScreenのプレビュー - エラーダイアログ */
@Preview(showBackground = true)
@Composable
fun LoginScreenErrorDialogPreview() {
  MaterialTheme {
    LoginScreen(
        uiState = LoginUiState(errorMessage = "ログインがキャンセルされました。再度お試しください。"),
        onLoginButtonClick = {})
  }
}

/** LoginScreenのプレビュー - タイムアウトダイアログ */
@Preview(showBackground = true)
@Composable
fun LoginScreenTimeoutDialogPreview() {
  MaterialTheme {
    LoginScreen(
        uiState = LoginUiState(errorMessage = "ログイン処理がタイムアウトしました。Amberアプリを確認して再試行してください。"),
        onLoginButtonClick = {})
  }
}

/** LoginScreenのプレビュー - ログイン成功 (Task 8.3) */
@Preview(showBackground = true)
@Composable
fun LoginScreenSuccessPreview() {
  MaterialTheme {
    LoginScreen(uiState = LoginUiState(loginSuccess = true), onLoginButtonClick = {})
  }
}
