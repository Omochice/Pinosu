package io.github.omochice.pinosu.data.repository

import android.content.Intent
import io.github.omochice.pinosu.domain.model.User

/**
 * Authentication repository interface
 *
 * Integrates AmberSignerClient and LocalAuthDataSource to provide authentication flow and local
 * state management.
 *
 * Task 5.1: AuthRepository implementation Requirements: 1.3, 1.4, 2.1, 2.2, 2.4, 2.5
 */
interface AuthRepository {

  /**
   * Get login state
   *
   * Retrieves saved user information from LocalAuthDataSource.
   *
   * Task 5.1: getLoginState() implementation Requirement 2.2: Login state verification
   *
   * @return User if logged in, null if not logged in
   */
  suspend fun getLoginState(): User?

  /**
   * Save login state
   *
   * Saves user information to LocalAuthDataSource.
   *
   * Task 5.1: saveLoginState() implementation Requirement 1.4: Login state persistence
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
   * Task 5.1: logout() implementation Requirement 2.4: Logout functionality
   *
   * @return Success on success, Failure(LogoutError) on failure
   */
  suspend fun logout(): Result<Unit>

  /**
   * Process Amber response and set user to logged-in state
   *
   * Parses response with AmberSignerClient and saves to LocalAuthDataSource on success.
   *
   * Task 5.1: processAmberResponse implementation Requirement 1.3, 1.4: Amber authentication and
   * local storage
   *
   * @param resultCode ActivityResult's resultCode
   * @param data Intent data
   * @return Success(User) on success, Failure(LoginError) on failure
   */
  suspend fun processAmberResponse(resultCode: Int, data: Intent?): Result<User>

  /**
   * Check if Amber app is installed
   *
   * Delegates to AmberSignerClient to verify Amber installation status.
   *
   * Task 5.1: checkAmberInstalled() implementation Requirement 1.2: Amber uninstalled detection
   *
   * @return true if Amber is installed
   */
  fun checkAmberInstalled(): Boolean
}
