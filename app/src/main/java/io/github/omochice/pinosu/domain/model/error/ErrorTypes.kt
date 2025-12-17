package io.github.omochice.pinosu.domain.model.error

/**
 * Login process errors
 *
 * Sealed class representing errors that can occur during the login process. Used by LoginUseCase
 * and AuthRepository.
 *
 * Task 2.2: Error type definition Requirements: 1.5, 4.5, 5.1, 5.2, 5.3, 5.4
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
 *
 * Task 2.2: Error type definition Requirements: 1.5, 4.5, 5.1, 5.2, 5.3, 5.4
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
 *
 * Task 2.2: Error type definition Requirements: 1.5, 4.5, 5.1, 5.2, 5.3, 5.4
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

/**
 * Amber communication errors
 *
 * Represents errors that can occur during NIP-55 Intent communication with the Amber app. Used by
 * AmberSignerClient.
 *
 * Task 2.2: Error type definition Requirements: 1.5, 4.5, 5.1, 5.2, 5.3, 5.4
 */
sealed class AmberError {
  /** Amber app is not installed */
  data object NotInstalled : AmberError()

  /** User rejected the operation in Amber */
  data object UserRejected : AmberError()

  /** Response from Amber timed out */
  data object Timeout : AmberError()

  /**
   * Response from Amber was in an invalid format
   *
   * @property message Error message
   */
  data class InvalidResponse(val message: String) : AmberError()

  /**
   * Failed to resolve Intent
   *
   * @property message Error message
   */
  data class IntentResolutionError(val message: String) : AmberError()
}
