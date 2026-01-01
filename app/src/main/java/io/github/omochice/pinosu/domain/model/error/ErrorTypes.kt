package io.github.omochice.pinosu.domain.model.error

/**
 * Login process errors
 *
 * Sealed class representing errors that can occur during the login process. Used by LoginUseCase
 * and AuthRepository.
 */
sealed class LoginError : Exception() {
  /** Amber app is not installed */
  data object AmberNotInstalled : LoginError()

  /** User rejected login in Amber */
  data object UserRejected : LoginError()

  /** Response from Amber timed out */
  data object Timeout : LoginError()

  /**
   * Network error occurred
   *
   * @property message Error message
   */
  data class NetworkError(override val message: String) : LoginError()

  /**
   * Unknown error occurred
   *
   * @property throwable The exception that occurred
   */
  data class UnknownError(val throwable: Throwable) : LoginError() {
    override val message: String
      get() = throwable.message ?: "Unknown error occurred"
  }
}

/**
 * Logout process errors
 *
 * Sealed class representing errors that can occur during the logout process. Used by LogoutUseCase.
 */
sealed class LogoutError : Exception() {
  /**
   * Error occurred during storage operation
   *
   * @property message Error message
   */
  data class StorageError(override val message: String) : LogoutError()
}

/**
 * Local storage operation errors
 *
 * Represents errors that can occur during local storage operations such as
 * EncryptedSharedPreferences. Used by LocalAuthDataSource and AuthRepository.
 */
sealed class StorageError : Exception() {
  /**
   * Failed to write to storage
   *
   * @property message Error message
   */
  data class WriteError(override val message: String) : StorageError()

  /**
   * Failed to read from storage
   *
   * @property message Error message
   */
  data class ReadError(override val message: String) : StorageError()
}
