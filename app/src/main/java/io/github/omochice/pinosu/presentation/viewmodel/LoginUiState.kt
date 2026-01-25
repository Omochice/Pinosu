package io.github.omochice.pinosu.presentation.viewmodel

/** Login screen UI state using sealed interface for type-safe state management */
sealed interface LoginUiState {
  /** Initial idle state, waiting for user action */
  data object Idle : LoginUiState

  /** Loading state during login process */
  data object Loading : LoginUiState

  /** Login completed successfully */
  data object Success : LoginUiState

  /** NIP-55 signer app is not installed */
  data object RequiresNip55Install : LoginUiState

  /** Error state with retryable/non-retryable distinction */
  sealed interface Error : LoginUiState {
    val message: String

    /** Retryable error (e.g., timeout) - shows retry button */
    data class Retryable(override val message: String) : Error

    /** Non-retryable error (e.g., user rejection) - shows OK button only */
    data class NonRetryable(override val message: String) : Error
  }
}
