package io.github.omochice.pinosu.feature.auth.domain.repository

import android.content.Intent
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.feature.auth.domain.model.LoginMode
import io.github.omochice.pinosu.feature.auth.domain.model.User

/**
 * Authentication repository interface
 *
 * Provides authentication flow and local state management.
 */
interface AuthRepository {

  /**
   * Get login state
   *
   * @return User if logged in, null if not logged in
   */
  suspend fun getLoginState(): User?

  /**
   * Save login state
   *
   * @param user User to save
   * @param loginMode How the user authenticated
   * @return Success on success, Failure(StorageError) on failure
   */
  suspend fun saveLoginState(user: User, loginMode: LoginMode): Result<Unit>

  /**
   * Retrieve stored login mode
   *
   * @return Stored login mode
   */
  suspend fun getLoginMode(): LoginMode

  /**
   * Save relay list
   *
   * @param relays List of relay configurations to save
   * @throws io.github.omochice.pinosu.feature.auth.domain.model.error.StorageError.WriteError when
   *   save fails
   */
  suspend fun saveRelayList(relays: List<RelayConfig>)

  /**
   * Logout
   *
   * @return Success on success, Failure(LogoutError) on failure
   */
  suspend fun logout(): Result<Unit>

  /**
   * Process NIP-55 signer response and set user to logged-in state
   *
   * @param resultCode ActivityResult's resultCode
   * @param data Intent data
   * @return Success(User) on success, Failure(LoginError) on failure
   */
  suspend fun processNip55Response(resultCode: Int, data: Intent?): Result<User>

  /**
   * Check if NIP-55 signer app is installed
   *
   * @return true if NIP-55 signer is installed
   */
  fun checkNip55SignerInstalled(): Boolean
}
