package io.github.omochice.pinosu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import io.github.omochice.pinosu.presentation.ui.LoginScreen
import io.github.omochice.pinosu.presentation.ui.MainScreen
import io.github.omochice.pinosu.presentation.viewmodel.LoginViewModel
import io.github.omochice.pinosu.ui.theme.PinosuTheme

/**
 * MainActivityクラス
 *
 * Task 10.1: アプリ起動時のログイン状態確認
 * - onCreate()でGetLoginStateUseCaseを呼び出し
 * - ログイン済み → メイン画面表示
 * - 未ログイン → ログイン画面表示
 * - 不正データ検出時のログイン状態クリア（UseCaseでnull返却として実装済み）
 *
 * Requirements: 2.2, 2.3
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val loginViewModel: LoginViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Task 10.1: ログイン状態を確認してUI状態を更新
    loginViewModel.checkLoginState()

    setContent { PinosuTheme { PinosuApp(viewModel = loginViewModel) } }
  }
}

/**
 * Pinosuアプリのメインコンポーザブル
 *
 * ログイン状態に応じてLoginScreenまたはMainScreenを表示する
 *
 * Task 10.1: アプリ起動時のログイン状態確認 Requirements: 2.2, 2.3
 */
@Composable
fun PinosuApp(viewModel: LoginViewModel) {
  // MainUiStateを観察してログイン状態を判定
  val mainUiState by viewModel.mainUiState.collectAsState()
  val loginUiState by viewModel.uiState.collectAsState()

  // Task 10.1: ログイン状態に基づいて表示画面を決定
  // Requirement 2.2: アプリ起動時に保存されたログイン状態確認
  // Requirement 2.3: ログイン済み状態でメイン画面表示
  if (mainUiState.userPubkey != null) {
    // ログイン済み → メイン画面表示
    MainScreen(
        uiState = mainUiState,
        onLogout = { viewModel.onLogoutButtonClicked() },
        onNavigateToLogin = {
          // ログアウト後は自動的にログイン画面に戻る（MainScreenのLaunchedEffectで制御）
        })
  } else {
    // 未ログイン → ログイン画面表示
    LoginScreen(
        uiState = loginUiState,
        onLoginButtonClick = { viewModel.onLoginButtonClicked() },
        onDismissDialog = { viewModel.dismissError() },
        onInstallAmber = {
          // TODO: Task 10.3でPlay Storeへのリンク実装予定
        },
        onRetry = { viewModel.onRetryLogin() },
        onNavigateToMain = {
          // ログイン成功時は自動的にメイン画面に遷移（LoginScreenのLaunchedEffectで制御）
        })
  }
}
