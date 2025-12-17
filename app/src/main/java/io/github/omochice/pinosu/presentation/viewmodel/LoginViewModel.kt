package io.github.omochice.pinosu.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.data.repository.AuthRepository
import io.github.omochice.pinosu.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.domain.usecase.LogoutUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for login/logout screen
 *
 * Task 7.1: LoginViewModel implementation
 * - UI state management (LoginUiState, MainUiState)
 * - User interaction handling
 * - Delegation to UseCases
 *
 * Task 7.2: Coroutine execution and error handling
 * - Amber response processing
 * - Loading state management
 * - StateFlow updates on error
 *
 * Requirements: 1.1, 1.5, 2.2, 2.3, 2.4, 3.2, 3.3, 3.5, 5.2, 5.4
 *
 * @property loginUseCase UseCase for login processing
 * @property logoutUseCase UseCase for logout processing
 * @property getLoginStateUseCase UseCase for retrieving login state
 * @property authRepository Authentication repository (for Amber response processing)
 */
@HiltViewModel
class LoginViewModel
@Inject
constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getLoginStateUseCase: GetLoginStateUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {

  // ========== Login screen UI state ==========

  private val _uiState = MutableStateFlow(LoginUiState())
  val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

  // ========== Main screen UI state ==========

  private val _mainUiState = MutableStateFlow(MainUiState())
  val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

  // ========== Check login state ==========

  /**
   * Check login state and update MainUiState
   *
   * Task 7.1: checkLoginState() implementation Requirement 2.2, 2.3: Check login state on app
   * startup
   */
  fun checkLoginState() {
    viewModelScope.launch {
      val user = getLoginStateUseCase()
      _mainUiState.value = MainUiState(userPubkey = user?.pubkey)
    }
  }

  // ========== Login button click ==========

  /**
   * Handle login button click
   *
   * Check Amber installation status and display dialog if not installed.
   *
   * Task 7.1: onLoginButtonClicked() implementation Requirement 1.1, 1.2: Login with Amber, detect
   * not installed
   */
  fun onLoginButtonClicked() {
    val isAmberInstalled = loginUseCase.checkAmberInstalled()
    if (!isAmberInstalled) {
      _uiState.value =
          _uiState.value.copy(showAmberInstallDialog = true, errorMessage = null, isLoading = false)
    }
    // Note: Intent launch for installed Amber uses ActivityResultAPI,
    // so it's handled in UI layer (Activity/Fragment) rather than ViewModel
  }

  // ========== Logout button click ==========

  /**
   * Handle logout button click
   *
   * Task 7.1: onLogoutButtonClicked() implementation Requirement 2.4: Logout functionality
   */
  fun onLogoutButtonClicked() {
    viewModelScope.launch {
      _mainUiState.value = _mainUiState.value.copy(isLoggingOut = true)
      val result = logoutUseCase()
      if (result.isSuccess) {
        _mainUiState.value = MainUiState(userPubkey = null, isLoggingOut = false)
      } else {
        // Handle logout failure (currently only reset state)
        _mainUiState.value = _mainUiState.value.copy(isLoggingOut = false)
      }
    }
  }

  // ========== Dismiss error ==========

  /**
   * Dismiss error message or dialog
   *
   * Task 7.1: dismissError() implementation Requirement 1.5: User feedback on error
   */
  fun dismissError() {
    _uiState.value =
        _uiState.value.copy(
            errorMessage = null, showAmberInstallDialog = false, loginSuccess = false)
  }

  // ========== Retry login ==========

  /**
   * Retry login
   *
   * Task 7.1: onRetryLogin() implementation Requirement 5.4: Retry option on timeout
   */
  fun onRetryLogin() {
    dismissError()
    onLoginButtonClicked()
  }

  // ========== Amber response processing (Task 7.2) ==========

  /**
   * Process response after receiving ActivityResult from Amber
   *
   * Task 7.2: processAmberResponse() implementation Requirement 1.3, 1.4, 1.5: Amber auth response
   * processing, save login state, error handling Requirement 3.2, 3.3: Loading state management,
   * display login success
   *
   * @param resultCode ActivityResult resultCode
   * @param data Intent data
   */
  fun processAmberResponse(resultCode: Int, data: android.content.Intent?) {
    viewModelScope.launch {
      // Start loading
      _uiState.value =
          _uiState.value.copy(isLoading = true, errorMessage = null, loginSuccess = false)

      // Process Amber response
      val result = authRepository.processAmberResponse(resultCode, data)

      if (result.isSuccess) {
        // Login successful
        val user = result.getOrNull()
        _uiState.value =
            _uiState.value.copy(isLoading = false, loginSuccess = true, errorMessage = null)
        _mainUiState.value = MainUiState(userPubkey = user?.pubkey)
      } else {
        // Login failed - set error message
        val error = result.exceptionOrNull()
        val errorMessage =
            when (error) {
              is io.github.omochice.pinosu.domain.model.error.LoginError.UserRejected ->
                  "Login was cancelled. Please try again."
              is io.github.omochice.pinosu.domain.model.error.LoginError.Timeout ->
                  "Login process timed out. Please check the Amber app and retry."
              is io.github.omochice.pinosu.domain.model.error.LoginError.NetworkError ->
                  "A network error occurred. Please check your connection."
              is io.github.omochice.pinosu.domain.model.error.LoginError.UnknownError ->
                  "An error occurred. Please try again later."
              else -> "An error occurred. Please try again later."
            }
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, errorMessage = errorMessage, loginSuccess = false)
      }
    }
  }
}

// ========== UI state data classes ==========

/**
 * Login screen UI state
 *
 * Task 7.1: LoginUiState definition Requirement 3.2, 3.3: Loading, error, success display
 *
 * @property isLoading Whether currently loading
 * @property errorMessage Error message
 * @property showAmberInstallDialog Whether to show Amber not installed dialog
 * @property loginSuccess Whether login was successful
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showAmberInstallDialog: Boolean = false,
    val loginSuccess: Boolean = false,
)

/**
 * Main screen UI state
 *
 * Task 7.1: MainUiState definition Requirement 3.5: Display logged-in pubkey
 *
 * @property userPubkey Logged-in user's public key
 * @property isLoggingOut Whether logout process is in progress
 */
data class MainUiState(
    val userPubkey: String? = null,
    val isLoggingOut: Boolean = false,
)
