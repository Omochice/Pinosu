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
 * @property loginUseCase UseCase for login processing
 * @property logoutUseCase UseCase for logout processing
 * @property getLoginStateUseCase UseCase for retrieving login state
 * @property authRepository Authentication repository (for NIP-55 signer response processing)
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

  private val _uiState = MutableStateFlow(LoginUiState())
  val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

  private val _mainUiState = MutableStateFlow(MainUiState())
  val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

  /** Check and restore login state from local storage */
  fun checkLoginState() {
    viewModelScope.launch {
      val user = getLoginStateUseCase()
      _mainUiState.value = MainUiState(userPubkey = user?.pubkey)
    }
  }

  /** Handle login button click, checking NIP-55 signer installation status */
  fun onLoginButtonClicked() {
    val isNip55SignerInstalled = loginUseCase.checkNip55SignerInstalled()
    if (!isNip55SignerInstalled) {
      _uiState.value =
          _uiState.value.copy(showNip55InstallDialog = true, errorMessage = null, isLoading = false)
    }
  }

  /** Handle logout button click, clearing login state */
  fun onLogoutButtonClicked() {
    viewModelScope.launch {
      _mainUiState.value = _mainUiState.value.copy(isLoggingOut = true)
      val result = logoutUseCase()
      if (result.isSuccess) {
        _mainUiState.value = MainUiState(userPubkey = null, isLoggingOut = false)
      } else {
        _mainUiState.value = _mainUiState.value.copy(isLoggingOut = false)
      }
    }
  }

  /** Dismiss error dialog and reset error state */
  fun dismissError() {
    _uiState.value =
        _uiState.value.copy(
            errorMessage = null, showNip55InstallDialog = false, loginSuccess = false)
  }

  /** Retry login after an error occurred */
  fun onRetryLogin() {
    dismissError()
    onLoginButtonClicked()
  }

  /**
   * Process response after receiving ActivityResult from NIP-55 signer
   *
   * @param resultCode ActivityResult resultCode
   * @param data Intent data
   */
  fun processNip55Response(resultCode: Int, data: android.content.Intent?) {
    viewModelScope.launch {
      _uiState.value =
          _uiState.value.copy(isLoading = true, errorMessage = null, loginSuccess = false)

      val result = authRepository.processNip55Response(resultCode, data)

      if (result.isSuccess) {
        val user = result.getOrNull()
        _uiState.value =
            _uiState.value.copy(isLoading = false, loginSuccess = true, errorMessage = null)
        _mainUiState.value = MainUiState(userPubkey = user?.pubkey)
      } else {
        val error = result.exceptionOrNull()
        val errorMessage =
            when (error) {
              is io.github.omochice.pinosu.domain.model.error.LoginError.UserRejected ->
                  "Login was cancelled. Please try again."
              is io.github.omochice.pinosu.domain.model.error.LoginError.Timeout ->
                  "Login process timed out. Please check the NIP-55 signer app and retry."
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

/**
 * Login screen UI state
 *
 * @property isLoading Whether currently loading
 * @property errorMessage Error message
 * @property showNip55InstallDialog Whether to show NIP-55 signer not installed dialog
 * @property loginSuccess Whether login was successful
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showNip55InstallDialog: Boolean = false,
    val loginSuccess: Boolean = false,
)

/**
 * Main screen UI state
 *
 * @property userPubkey Logged-in user's public key
 * @property isLoggingOut Whether logout process is in progress
 */
data class MainUiState(
    val userPubkey: String? = null,
    val isLoggingOut: Boolean = false,
)
