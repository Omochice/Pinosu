package io.github.omochice.pinosu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.domain.usecase.LogoutUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ログイン/ログアウト画面のViewModel
 *
 * Task 7.1: LoginViewModelの実装
 * - UI状態管理（LoginUiState, MainUiState）
 * - ユーザー操作ハンドリング
 * - UseCasesへの委譲
 *
 * Requirements: 1.1, 1.5, 2.2, 2.3, 2.4, 3.2, 3.3, 3.5
 *
 * @property loginUseCase ログイン処理のUseCase
 * @property logoutUseCase ログアウト処理のUseCase
 * @property getLoginStateUseCase ログイン状態取得のUseCase
 */
@HiltViewModel
class LoginViewModel
@Inject
constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getLoginStateUseCase: GetLoginStateUseCase,
) : ViewModel() {

  // ========== ログイン画面のUI状態 ==========

  private val _uiState = MutableStateFlow(LoginUiState())
  val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

  // ========== メイン画面のUI状態 ==========

  private val _mainUiState = MutableStateFlow(MainUiState())
  val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

  // ========== ログイン状態確認 ==========

  /**
   * ログイン状態を確認してMainUiStateを更新する
   *
   * Task 7.1: checkLoginState()実装 Requirement 2.2, 2.3: アプリ起動時のログイン状態確認
   */
  fun checkLoginState() {
    viewModelScope.launch {
      val user = getLoginStateUseCase()
      _mainUiState.value = MainUiState(userPubkey = user?.pubkey)
    }
  }

  // ========== ログインボタンクリック ==========

  /**
   * ログインボタンがクリックされた時の処理
   *
   * Amberのインストール状態を確認し、未インストールの場合はダイアログを表示する。
   *
   * Task 7.1: onLoginButtonClicked()実装 Requirement 1.1, 1.2: Amberでログイン、未インストール検出
   */
  fun onLoginButtonClicked() {
    val isAmberInstalled = loginUseCase.checkAmberInstalled()
    if (!isAmberInstalled) {
      _uiState.value =
          _uiState.value.copy(showAmberInstallDialog = true, errorMessage = null, isLoading = false)
    }
    // Note: Amberインストール済みの場合のIntent起動はActivityResultAPIを使用するため、
    // ViewModel内では処理せず、UI層（Activity/Fragment）で処理する
  }

  // ========== ログアウトボタンクリック ==========

  /**
   * ログアウトボタンがクリックされた時の処理
   *
   * Task 7.1: onLogoutButtonClicked()実装 Requirement 2.4: ログアウト機能
   */
  fun onLogoutButtonClicked() {
    viewModelScope.launch {
      _mainUiState.value = _mainUiState.value.copy(isLoggingOut = true)
      val result = logoutUseCase()
      if (result.isSuccess) {
        _mainUiState.value = MainUiState(userPubkey = null, isLoggingOut = false)
      } else {
        // ログアウト失敗時の処理（現時点では状態をリセットのみ）
        _mainUiState.value = _mainUiState.value.copy(isLoggingOut = false)
      }
    }
  }

  // ========== エラー解除 ==========

  /**
   * エラーメッセージやダイアログを解除する
   *
   * Task 7.1: dismissError()実装 Requirement 1.5: エラー時のユーザーフィードバック
   */
  fun dismissError() {
    _uiState.value =
        _uiState.value.copy(
            errorMessage = null, showAmberInstallDialog = false, loginSuccess = false)
  }

  // ========== ログイン再試行 ==========

  /**
   * ログインを再試行する
   *
   * Task 7.1: onRetryLogin()実装 Requirement 5.4: タイムアウト時の再試行オプション
   */
  fun onRetryLogin() {
    dismissError()
    onLoginButtonClicked()
  }
}

// ========== UI状態データクラス ==========

/**
 * ログイン画面のUI状態
 *
 * Task 7.1: LoginUiState定義 Requirement 3.2, 3.3: ローディング、エラー、成功表示
 *
 * @property isLoading ローディング中かどうか
 * @property errorMessage エラーメッセージ
 * @property showAmberInstallDialog Amber未インストールダイアログを表示するか
 * @property loginSuccess ログインに成功したかどうか
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showAmberInstallDialog: Boolean = false,
    val loginSuccess: Boolean = false,
)

/**
 * メイン画面のUI状態
 *
 * Task 7.1: MainUiState定義 Requirement 3.5: ログイン中のpubkey表示
 *
 * @property userPubkey ログイン中のユーザー公開鍵
 * @property isLoggingOut ログアウト処理中かどうか
 */
data class MainUiState(
    val userPubkey: String? = null,
    val isLoggingOut: Boolean = false,
)
