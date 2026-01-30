package io.github.omochice.pinosu.feature.auth.data.repository

import android.content.Intent
import io.github.omochice.pinosu.feature.auth.domain.model.User

/**
 * Authentication repository interface
 *
 * Integrates Nip55SignerClient and LocalAuthDataSource to provide authentication flow and local
 * state management.
 */
interface AuthRepository {

  /**
   * Get login state
   *
   * Retrieves saved user information from LocalAuthDataSource.
   *
   * @return User if logged in, null if not logged in
   */
  suspend fun getLoginState(): User?

  /**
   * Save login state
   *
   * Saves user information to LocalAuthDataSource.
   *
   * @param user User to save
   * @return Success on success, Failure(StorageError) on failure
   */
  suspend fun saveLoginState(user: User): Result<Unit>

  /**
   * Logout
   *
   * Clears login state in LocalAuthDataSource.
   *
   * @return Success on success, Failure(LogoutError) on failure
   */
  suspend fun logout(): Result<Unit>

  /**
   * Process NIP-55 signer response and set user to logged-in state
   *
   * Parses response with Nip55SignerClient and saves to LocalAuthDataSource on success.
   *
   * @param resultCode ActivityResult's resultCode
   * @param data Intent data
   * @return Success(User) on success, Failure(LoginError) on failure
   */
  suspend fun processNip55Response(resultCode: Int, data: Intent?): Result<User>

  /**
   * Check if NIP-55 signer app is installed
   *
   * Delegates to Nip55SignerClient to verify NIP-55 signer installation status.
   *
   * @return true if NIP-55 signer is installed
   */
  fun checkNip55SignerInstalled(): Boolean
}
