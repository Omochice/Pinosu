package io.github.omochice.pinosu.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * Requirements: 3.1, 3.2
 *
 * @param uiState ログイン画面のUI状態
 * @param onLoginButtonClick ログインボタンクリック時のコールバック
 */
@Composable
fun LoginScreen(uiState: LoginUiState, onLoginButtonClick: () -> Unit) {
  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(16.dp)) {
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
