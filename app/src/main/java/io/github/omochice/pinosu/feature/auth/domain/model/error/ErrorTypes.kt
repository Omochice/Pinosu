package io.github.omochice.pinosu.feature.auth.domain.model.error

/**
 * Login process errors
 *
 * Sealed class representing errors that can occur during the login process. Used by LoginUseCase
 * and AuthRepository.
 */
sealed class LoginError : Exception() {
  /** NIP-55 signer app is not installed */
  data object Nip55SignerNotInstalled : LoginError()

  /** User rejected login in NIP-55 signer */
  data object UserRejected : LoginError()

  /** Response from NIP-55 signer timed out */
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
 * Represents errors that can occur during local storage operations such as DataStore. Used by
 * LocalAuthDataSource and AuthRepository.
 */
sealed class StorageError : Exception() {
  /**
   * Failed to write to storage
   *
   * @property message Error message
   * @property cause Original exception that caused the write failure
   */
  data class WriteError(override val message: String, override val cause: Throwable? = null) :
      StorageError()

  /**
   * Failed to read from storage
   *
   * @property message Error message
   */
  data class ReadError(override val message: String) : StorageError()
}
