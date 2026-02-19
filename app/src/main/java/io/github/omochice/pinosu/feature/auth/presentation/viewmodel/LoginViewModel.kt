package io.github.omochice.pinosu.feature.auth.presentation.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.feature.auth.data.repository.AuthRepository
import io.github.omochice.pinosu.feature.auth.domain.model.error.LoginError
import io.github.omochice.pinosu.feature.auth.domain.usecase.FetchRelayListUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.GetLoginStateUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.LoginUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.LogoutUseCase
import io.github.omochice.pinosu.feature.auth.domain.usecase.ReadOnlyLoginUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for login/logout screen
 *
 * @param loginUseCase UseCase for login operations
 * @param logoutUseCase UseCase for logout operations
 * @param getLoginStateUseCase UseCase for retrieving current login state
 * @param authRepository Repository for processing NIP-55 signer responses
 * @param fetchRelayListUseCase UseCase for fetching NIP-65 relay list after login
 * @param readOnlyLoginUseCase UseCase for read-only npub login
 * @param localAuthDataSource Local data source for reading login mode
 */
@HiltViewModel
class LoginViewModel
@Inject
constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getLoginStateUseCase: GetLoginStateUseCase,
    private val authRepository: AuthRepository,
    private val fetchRelayListUseCase: FetchRelayListUseCase,
    private val readOnlyLoginUseCase: ReadOnlyLoginUseCase,
    private val localAuthDataSource: LocalAuthDataSource,
) : ViewModel() {

  private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
  val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

  private val _mainUiState = MutableStateFlow(MainUiState())
  val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

  /** Check and restore login state from local storage */
  fun checkLoginState() {
    viewModelScope.launch {
      val user = getLoginStateUseCase()
      val loginMode = localAuthDataSource.getLoginMode()
      _mainUiState.value =
          MainUiState(userPubkey = user?.pubkey?.npub, isReadOnly = loginMode.isReadOnly)
    }
  }

  /** Handle login button click, checking NIP-55 signer installation status */
  fun onLoginButtonClicked() {
    val isNip55SignerInstalled = loginUseCase.checkNip55SignerInstalled()
    if (!isNip55SignerInstalled) {
      _uiState.value = LoginUiState.RequiresNip55Install
    }
  }

  /** Handle logout button click, clearing login state */
  fun onLogoutButtonClicked() {
    viewModelScope.launch {
      _mainUiState.value = _mainUiState.value.copy(isLoggingOut = true)
      val result = logoutUseCase()
      if (result.isSuccess) {
        _mainUiState.value = MainUiState()
      } else {
        _mainUiState.value = _mainUiState.value.copy(isLoggingOut = false)
      }
    }
  }

  /**
   * Handle read-only login submission with npub
   *
   * @param npub Nostr public key in npub format
   */
  fun onReadOnlyLoginSubmit(npub: String) {
    viewModelScope.launch {
      _uiState.value = LoginUiState.Loading

      val result = readOnlyLoginUseCase(npub)

      if (result.isSuccess) {
        val user = result.getOrNull()
        _mainUiState.value = MainUiState(userPubkey = user?.pubkey?.npub, isReadOnly = true)
        _uiState.value = LoginUiState.Success
      } else {
        val error = result.exceptionOrNull()
        _uiState.value =
            when (error) {
              is LoginError.InvalidPubkey ->
                  LoginUiState.Error.NonRetryable("Invalid public key format.")
              else -> LoginUiState.Error.NonRetryable("An error occurred. Please try again later.")
            }
      }
    }
  }

  /** Dismiss any login-related dialog and reset the login UI state to Idle */
  fun dismissError() {
    _uiState.value = LoginUiState.Idle
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
  fun processNip55Response(resultCode: Int, data: Intent?) {
    viewModelScope.launch {
      _uiState.value = LoginUiState.Loading

      val result = authRepository.processNip55Response(resultCode, data)

      if (result.isSuccess) {
        val user = result.getOrNull()

        // Fetch NIP-65 relay list and wait for completion before login success
        user?.pubkey?.let { pubkey ->
          val relayResult = fetchRelayListUseCase(pubkey.npub)
          if (relayResult.isFailure) {
            Log.w(
                TAG, "Failed to fetch NIP-65 relay list: ${relayResult.exceptionOrNull()?.message}")
          }
        }

        _mainUiState.value = MainUiState(userPubkey = user?.pubkey?.npub)
        _uiState.value = LoginUiState.Success
      } else {
        val error = result.exceptionOrNull()
        _uiState.value =
            when (error) {
              is LoginError.UserRejected ->
                  LoginUiState.Error.NonRetryable("Login was cancelled. Please try again.")
              is LoginError.Timeout ->
                  LoginUiState.Error.Retryable(
                      "Login process timed out. Please check the NIP-55 signer app and retry.")
              is LoginError.NetworkError ->
                  LoginUiState.Error.Retryable(
                      "A network error occurred. Please check your connection.")
              is LoginError.UnknownError ->
                  LoginUiState.Error.NonRetryable("An error occurred. Please try again later.")
              else -> LoginUiState.Error.NonRetryable("An error occurred. Please try again later.")
            }
      }
    }
  }

  companion object {
    private const val TAG = "LoginViewModel"
  }
}
