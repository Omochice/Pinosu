package io.github.omochice.pinosu.data.repository

import android.content.Intent
import io.github.omochice.pinosu.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.data.nip55.Nip55Error
import io.github.omochice.pinosu.data.nip55.Nip55SignerClient
import io.github.omochice.pinosu.domain.model.User
import io.github.omochice.pinosu.domain.model.error.LoginError
import io.github.omochice.pinosu.domain.model.error.LogoutError
import io.github.omochice.pinosu.domain.model.error.StorageError
import javax.inject.Inject

/**
 * NIP-55 based AuthRepository implementation
 *
 * Integrates Nip55SignerClient and LocalAuthDataSource to provide authentication flow and local
 * state management.
 *
 * @property nip55SignerClient NIP-55 signer communication client
 * @property localAuthDataSource Local storage data source
 */
class Nip55AuthRepository
@Inject
constructor(
    private val nip55SignerClient: Nip55SignerClient,
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
   * Process NIP-55 signer response and set user to logged-in state
   *
   * @param resultCode ActivityResult's resultCode
   * @param data Intent data
   * @return Success(User) on success, Failure(LoginError) on failure
   */
  override suspend fun processNip55Response(resultCode: Int, data: Intent?): Result<User> {
    val nip55Result = nip55SignerClient.handleNip55Response(resultCode, data)

    return if (nip55Result.isSuccess) {
      val nip55Response = nip55Result.getOrNull()!!
      val user = User(nip55Response.pubkey)

      try {
        localAuthDataSource.saveUser(user)
        Result.success(user)
      } catch (e: StorageError) {
        Result.failure(LoginError.UnknownError(e))
      }
    } else {
      val nip55Error = nip55Result.exceptionOrNull() as? Nip55Error
      val loginError =
          when (nip55Error) {
            is Nip55Error.NotInstalled -> LoginError.Nip55SignerNotInstalled
            is Nip55Error.UserRejected -> LoginError.UserRejected
            is Nip55Error.Timeout -> LoginError.Timeout
            is Nip55Error.InvalidResponse,
            is Nip55Error.IntentResolutionError -> LoginError.NetworkError(nip55Error.toString())
            null -> LoginError.UnknownError(Exception("Unknown NIP-55 signer error"))
          }
      Result.failure(loginError)
    }
  }

  /**
   * Check if NIP-55 signer app is installed
   *
   * @return true if NIP-55 signer is installed
   */
  override fun checkNip55SignerInstalled(): Boolean {
    return nip55SignerClient.checkNip55SignerInstalled()
  }
}
