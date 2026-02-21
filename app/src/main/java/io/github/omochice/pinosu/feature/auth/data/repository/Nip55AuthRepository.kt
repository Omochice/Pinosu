package io.github.omochice.pinosu.feature.auth.data.repository

import android.content.Intent
import io.github.omochice.pinosu.core.model.Pubkey
import io.github.omochice.pinosu.core.nip.nip55.Nip55Error
import io.github.omochice.pinosu.core.nip.nip55.Nip55SignerClient
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import io.github.omochice.pinosu.feature.auth.domain.model.LoginMode
import io.github.omochice.pinosu.feature.auth.domain.model.User
import io.github.omochice.pinosu.feature.auth.domain.model.error.LoginError
import io.github.omochice.pinosu.feature.auth.domain.model.error.LogoutError
import io.github.omochice.pinosu.feature.auth.domain.model.error.StorageError
import io.github.omochice.pinosu.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * NIP-55 based [AuthRepository] implementation
 *
 * Integrates [Nip55SignerClient] and [LocalAuthDataSource] to provide authentication flow and local
 * state management.
 *
 * @param nip55SignerClient Client for NIP-55 signer interaction
 * @param localAuthDataSource Local data source for user authentication state
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
  override suspend fun getLoginState(): User? = localAuthDataSource.getUser()

  /**
   * Save login state
   *
   * @param user User to save
   * @param loginMode How the user authenticated
   * @return Success on success, Failure(StorageError) on failure
   */
  override suspend fun saveLoginState(user: User, loginMode: LoginMode): Result<Unit> {
    return try {
      localAuthDataSource.saveUser(user, loginMode)
      Result.success(Unit)
    } catch (e: StorageError) {
      Result.failure(e)
    }
  }

  override suspend fun getLoginMode(): LoginMode = localAuthDataSource.getLoginMode()

  override suspend fun saveRelayList(
      relays: List<io.github.omochice.pinosu.core.relay.RelayConfig>
  ) = localAuthDataSource.saveRelayList(relays)

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
      val pubkey =
          Pubkey.parse(nip55Response.pubkey)
              ?: return Result.failure(LoginError.NetworkError("Invalid pubkey format from signer"))
      val user = User(pubkey)

      try {
        localAuthDataSource.saveUser(user, LoginMode.Nip55Signer)
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
            is Nip55Error.InvalidResponse -> LoginError.NetworkError(nip55Error.message)
            is Nip55Error.IntentResolutionError -> LoginError.NetworkError(nip55Error.message)
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
  override fun checkNip55SignerInstalled(): Boolean = nip55SignerClient.checkNip55SignerInstalled()
}
