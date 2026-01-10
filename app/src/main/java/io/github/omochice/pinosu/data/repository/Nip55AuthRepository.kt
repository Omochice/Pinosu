package io.github.omochice.pinosu.data.repository

import android.content.Intent
import io.github.omochice.pinosu.data.amber.AmberError
import io.github.omochice.pinosu.data.amber.AmberSignerClient
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.model.error.LoginError
import io.github.omochice.pinosu.domain.model.error.LogoutError
import io.github.omochice.pinosu.domain.model.error.StorageError
import javax.inject.Inject

/**
 * Amber-based AuthRepository implementation
 *
 * Integrates AmberSignerClient and LocalAuthDataSource to provide authentication flow and local
 * state management.
 *
 * @property amberSignerClient Amber communication client
 * @property localAuthDataSource Local storage data source
 */
class AmberAuthRepository
@Inject
constructor(
    private val amberSignerClient: AmberSignerClient,
    private val localAuthDataSource: LocalAuthDataSource
) : AuthRepository {

  /**
   * Get login state
   *
   * @return User if logged in, null if not logged in
   */
  override suspend fun getLoginState(): User? {
    return localAuthDataSource.getUser()
  }

  /**
   * Save login state
   *
   * @param user User to save
   * @return Success on success, Failure(StorageError) on failure
   */
  override suspend fun saveLoginState(user: User): Result<Unit> {
    return try {
      localAuthDataSource.saveUser(user)
      Result.success(Unit)
    } catch (e: StorageError) {
      Result.failure(e)
    }
  }

  /**
   * Logout
   *
   * @return Success on success, Failure(LogoutError) on failure
   */
  override suspend fun logout(): Result<Unit> {
    return try {
      localAuthDataSource.clearLoginState()
      Result.success(Unit)
    } catch (e: StorageError) {
      Result.failure(LogoutError.StorageError(e.message ?: "Failed to clear login state"))
    }
  }

  /**
   * Process Amber response and set user to logged-in state
   *
   * @param resultCode ActivityResult's resultCode
   * @param data Intent data
   * @return Success(User) on success, Failure(LoginError) on failure
   */
  override suspend fun processAmberResponse(resultCode: Int, data: Intent?): Result<User> {
    val amberResult = amberSignerClient.handleAmberResponse(resultCode, data)

    return if (amberResult.isSuccess) {
      val amberResponse = amberResult.getOrNull()!!
      val user = User(amberResponse.pubkey)

      try {
        localAuthDataSource.saveUser(user)
        Result.success(user)
      } catch (e: StorageError) {
        Result.failure(LoginError.UnknownError(e))
      }
    } else {
      val amberError = amberResult.exceptionOrNull() as? AmberError
      val loginError =
          when (amberError) {
            is AmberError.NotInstalled -> LoginError.AmberNotInstalled
            is AmberError.UserRejected -> LoginError.UserRejected
            is AmberError.Timeout -> LoginError.Timeout
            is AmberError.InvalidResponse,
            is AmberError.IntentResolutionError ->
                LoginError.NetworkError(amberError?.toString() ?: "Unknown Amber error")
            null -> LoginError.UnknownError(Exception("Unknown Amber error"))
          }
      Result.failure(loginError)
    }
  }

  /**
   * Check if Amber app is installed
   *
   * @return true if Amber is installed
   */
  override fun checkAmberInstalled(): Boolean {
    return amberSignerClient.checkAmberInstalled()
  }
}
