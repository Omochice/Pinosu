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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.omochice.pinosu.presentation.viewmodel.MainUiState

/**
 * メイン画面のUI
 *
 * Task 9.1: MainScreenの基本実装
 * - ログアウトボタンの配置
 * - ユーザーpubkeyの表示（部分マスキング）
 * - ログアウト処理中のローディング表示
 *
 * Task 9.2: ログアウト処理とナビゲーション
 * - ログアウト完了後のログイン画面への遷移
 *
 * Requirements: 2.3, 2.4, 3.4, 3.5
 *
 * @param uiState メイン画面のUI状態
 * @param onLogout ログアウトボタンがタップされた時のコールバック
 * @param onNavigateToLogin ログアウト完了後にログイン画面へ遷移するためのコールバック（Task 9.2）
 */
@Composable
fun MainScreen(
    uiState: MainUiState,
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
) {
  // Task 9.2: ログアウト完了検出 - pubkeyがnullになったらログイン画面へ遷移
  // 前回のpubkey状態を記憶し、ログイン中→ログアウト（null）への変化を検出
  var previousPubkey by remember { mutableStateOf(uiState.userPubkey) }

  LaunchedEffect(uiState.userPubkey) {
    // ログイン中（previousPubkey != null）からログアウト（userPubkey == null）に変化した場合
    if (previousPubkey != null && uiState.userPubkey == null && !uiState.isLoggingOut) {
      onNavigateToLogin()
    }
    previousPubkey = uiState.userPubkey
  }

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        // ログイン状態の表示
        if (uiState.userPubkey != null) {
          // ユーザー公開鍵の表示（Requirement 3.5: メイン画面にログイン中のpubkeyを表示）
          Text(
              text = "ログイン中",
              style = MaterialTheme.typography.headlineMedium,
              textAlign = TextAlign.Center)

          Spacer(modifier = Modifier.height(16.dp))

          // pubkeyの表示（部分マスキング推奨）
          Text(
              text = formatPubkey(uiState.userPubkey),
              style = MaterialTheme.typography.bodyMedium,
              textAlign = TextAlign.Center)

          Spacer(modifier = Modifier.height(32.dp))

          // ログアウトボタンまたはログアウト中メッセージ（Requirement 3.4: メイン画面にログアウトボタンを配置）
          if (uiState.isLoggingOut) {
            // ログアウト処理中はローディングメッセージを表示（Requirement 3.2: ローディングインジケーター）
            Text(
                text = "ログアウト中...",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center)
          } else {
            // ログアウトボタン
            Button(onClick = onLogout) { Text("ログアウト") }
          }
        } else {
          // 未ログイン状態（Requirement 2.3: ログイン済み状態の確認）
          Text(
              text = "ログインしていません",
              style = MaterialTheme.typography.headlineMedium,
              textAlign = TextAlign.Center)
        }
      }
}

/**
 * 公開鍵を表示用にフォーマットする
 *
 * design.md L329によると、pubkeyは部分的にマスキング表示推奨 最初の8文字と最後の8文字を表示し、中間を省略する
 *
 * @param pubkey Nostr公開鍵（64文字16進数）
 * @return フォーマットされた公開鍵文字列
 */
private fun formatPubkey(pubkey: String): String {
  return if (pubkey.length >= 16) {
    "${pubkey.take(8)}...${pubkey.takeLast(8)}"
  } else {
    pubkey
  }
}

// ========== プレビュー ==========

/** ログイン済み状態のMainScreenプレビュー */
@Preview(showBackground = true)
@Composable
private fun MainScreenLoggedInPreview() {
  val uiState =
      MainUiState(
          userPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
          isLoggingOut = false)
  MainScreen(uiState = uiState, onLogout = {})
}

/** ログアウト処理中のMainScreenプレビュー */
@Preview(showBackground = true)
@Composable
private fun MainScreenLoggingOutPreview() {
  val uiState =
      MainUiState(
          userPubkey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
          isLoggingOut = true)
  MainScreen(uiState = uiState, onLogout = {})
}

/** 未ログイン状態のMainScreenプレビュー */
@Preview(showBackground = true)
@Composable
private fun MainScreenNotLoggedInPreview() {
  val uiState = MainUiState(userPubkey = null, isLoggingOut = false)
  MainScreen(uiState = uiState, onLogout = {})
}
