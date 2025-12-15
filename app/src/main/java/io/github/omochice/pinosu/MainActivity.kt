package io.github.omochice.pinosu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.omochice.pinosu.presentation.navigation.LOGIN_ROUTE
import io.github.omochice.pinosu.presentation.navigation.MAIN_ROUTE
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
 * Task 10.2: Navigation Compose統合
 * - NavHostを使用した画面遷移管理
 * - ログイン状態に応じた初期ルート設定
 * - 画面遷移ロジックの実装
 *
 * Requirements: 2.2, 2.3, 3.3
 */
@Composable
fun PinosuApp(viewModel: LoginViewModel) {
  // NavControllerの作成
  val navController = rememberNavController()

  // MainUiStateを観察してログイン状態を判定
  val mainUiState by viewModel.mainUiState.collectAsState()
  val loginUiState by viewModel.uiState.collectAsState()

  // Task 10.2: ログイン状態に基づいて初期ルートを決定
  // Requirement 2.2: アプリ起動時に保存されたログイン状態確認
  // Requirement 2.3: ログイン済み状態でメイン画面表示
  val startDestination = if (mainUiState.userPubkey != null) MAIN_ROUTE else LOGIN_ROUTE

  NavHost(navController = navController, startDestination = startDestination) {
    // ログイン画面
    composable(LOGIN_ROUTE) {
      LoginScreen(
          uiState = loginUiState,
          onLoginButtonClick = { viewModel.onLoginButtonClicked() },
          onDismissDialog = { viewModel.dismissError() },
          onInstallAmber = {
            // TODO: Task 10.3でPlay Storeへのリンク実装予定
          },
          onRetry = { viewModel.onRetryLogin() },
          onNavigateToMain = {
            // Requirement 3.3: ログイン成功時にメイン画面への画面遷移
            navController.navigate(MAIN_ROUTE) {
              // ログイン画面をバックスタックから削除
              popUpTo(LOGIN_ROUTE) { inclusive = true }
            }
          })
    }

    // メイン画面
    composable(MAIN_ROUTE) {
      // Task 10.2: ログアウト完了検出とログイン画面への遷移
      LaunchedEffect(mainUiState.userPubkey) {
        if (mainUiState.userPubkey == null) {
          // Requirement 2.4: ログアウト後にログイン画面へ遷移
          navController.navigate(LOGIN_ROUTE) {
            // メイン画面をバックスタックから削除
            popUpTo(MAIN_ROUTE) { inclusive = true }
          }
        }
      }

      MainScreen(
          uiState = mainUiState,
          onLogout = { viewModel.onLogoutButtonClicked() },
          onNavigateToLogin = {
            // Note: ログアウト後の遷移はLaunchedEffectで自動実行される
          })
    }
  }
}
